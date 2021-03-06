/*
 *************************************************************************
 * Copyright (c) 2004, 2011 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *  
 *************************************************************************
 */
package org.eclipse.birt.data.engine.executor.transform;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.birt.core.archive.RAOutputStream;
import org.eclipse.birt.core.util.IOUtil;
import org.eclipse.birt.data.engine.api.DataEngineContext;
import org.eclipse.birt.data.engine.api.aggregation.IAggrFunction;
import org.eclipse.birt.data.engine.core.DataException;
import org.eclipse.birt.data.engine.executor.aggregation.ProgressiveAggregationHelper;
import org.eclipse.birt.data.engine.executor.transform.group.GroupBy;
import org.eclipse.birt.data.engine.executor.transform.group.GroupInfo;
import org.eclipse.birt.data.engine.impl.DataEngineSession;
import org.eclipse.birt.data.engine.impl.document.stream.StreamManager;
import org.eclipse.birt.data.engine.odi.IAggrInfo;
import org.eclipse.birt.data.engine.odi.IQuery.GroupSpec;
import org.eclipse.birt.data.engine.odi.IResultClass;
import org.eclipse.birt.data.engine.odi.IResultObject;


public class SimpleGroupCalculator implements IGroupCalculator
{
	private IResultObject previous;
	private IResultObject next;
	private IResultObject current;
	private GroupBy[] groupBys;
	private int[] groupInstanceIndex;
	private int[] latestAggrAvailableIndex;
	private StreamManager streamManager;
	private RAOutputStream[] groupOutput;
	private RAOutputStream[] aggrRAOutput;
	private DataOutputStream[] aggrOutput;
	private DataOutputStream[] aggrIndexOutput;
	//Output Stream for Overall index + Running index
	private RAOutputStream combinedAggrIndexRAOutput;
	private DataOutputStream combinedAggrIndexOutput;
	private RAOutputStream combinedAggrRAOutput;
	private DataOutputStream combinedAggrOutput;
	
	private ProgressiveAggregationHelper aggrHelper;
	private List<List<String>> groupAggrs;
	private List<String> runningAggrs;
	private List<String> overallAggrs;
	
	
	public SimpleGroupCalculator( DataEngineSession session, GroupSpec[] groups, IResultClass rsMeta ) throws DataException
	{
		this.groupBys = new GroupBy[groups.length];
		this.latestAggrAvailableIndex = new int[groups.length];
		Arrays.fill( this.latestAggrAvailableIndex, -1 );
		for ( int i = 0; i < groups.length; ++i )
		{
			int keyIndex = groups[i].getKeyIndex( );
			String keyColumn = groups[i].getKeyColumn( );

			// Convert group key name to index for faster future access
			// assume priority of keyColumn is higher than keyIndex
			if ( keyColumn != null )
				keyIndex = rsMeta.getFieldIndex( keyColumn );

			this.groupBys[i] = GroupBy.newInstance( groups[i],
					keyIndex,
					keyColumn,
					rsMeta.getFieldValueClass( keyIndex ) );
		}
		this.groupInstanceIndex = new int[groupBys.length];
		this.groupAggrs = new ArrayList<List<String>>();
		this.runningAggrs = new ArrayList<String>();
		this.overallAggrs = new ArrayList<String>();
		this.aggrOutput = new DataOutputStream[0];
		for( int i = 0; i < groups.length; i++ )
		{
			this.groupAggrs.add( new ArrayList<String>() );
		}
	}

	public void setAggrHelper( ProgressiveAggregationHelper aggrHelper ) throws DataException
	{
		this.aggrHelper = aggrHelper;
		for ( String aggrName : this.aggrHelper.getAggrNames( ) )
		{
			IAggrInfo aggrInfo = this.aggrHelper.getAggrInfo( aggrName );

			if ( aggrInfo.getAggregation( ).getType( ) == IAggrFunction.RUNNING_AGGR )
			{
				this.runningAggrs.add( aggrName );
			}
			else if ( aggrInfo.getGroupLevel( ) == 0 )
			{
				this.overallAggrs.add( aggrName );
			}
			else if ( this.aggrHelper.getAggrInfo( aggrName ).getGroupLevel( ) <= this.groupAggrs.size( ) )
			{
				this.groupAggrs.get( this.aggrHelper.getAggrInfo( aggrName )
						.getGroupLevel( ) - 1 ).add( aggrName );
			}
		}
	}
	
	private int getBreakingGroup( IResultObject obj1, IResultObject obj2 ) throws DataException
	{
		if( obj1 == null )
			return 0;
		
		if( obj2 == null )
			return 0;
		
		for( int i = 0; i < this.groupBys.length; i++ )
		{
			int columnIndex = groupBys[i].getColumnIndex( );
			if( !groupBys[i].isInSameGroup( obj1.getFieldValue( columnIndex ), obj2.getFieldValue( columnIndex ) ) )
			{
				return i + 1;
			}
		}
		
		return this.groupBys.length+1;
	}

	public int getStartingGroup( ) throws DataException
	{
		return this.getBreakingGroup( previous, current );
	}
	
	public int getEndingGroup( ) throws DataException
	{
		return this.getBreakingGroup( current, next );
	}

	public void registerPreviousResultObject( IResultObject previous )
	{
		this.previous = previous;
	}

	public void registerCurrentResultObject( IResultObject current )
	{
		this.current = current;
	}

	public void registerNextResultObject( IResultObject next )
	{
		this.next = next;
	}
	
	/**
	 * Do grouping, and fill group indexes
	 * 
	 * @param stopsign
	 * @throws DataException
	 */
	public void next( int rowId ) throws DataException
	{
		// breakLevel is the outermost group number to differentiate row
		// data
		int breakLevel;
		if ( this.previous == null )
			breakLevel = 0; // Special case for first row
		else
			breakLevel = getBreakLevel( this.current, this.previous );

		//String tobePrint = "";
		try
		{
			// Create a new group in each group level between
			// [ breakLevel ... groupDefs.length - 1]
			for ( int level = breakLevel; level < groupInstanceIndex.length; level++ )
			{
				GroupInfo group = new GroupInfo( );

				if ( level != 0 )
					group.parent = groupInstanceIndex[level - 1] - 1;
				if ( level == groupInstanceIndex.length - 1 )
				{
					// at leaf group level, first child is the first row, which
					// is current row
					group.firstChild = rowId;
				}
				else
				{
					// Our first child is the group to be created at the next
					// level
					// in the next loop
					group.firstChild = groupInstanceIndex[level + 1];
				}

				groupInstanceIndex[level]++;

				if ( this.streamManager != null )
				{

					IOUtil.writeInt( this.groupOutput[level], group.parent );
					IOUtil.writeInt( this.groupOutput[level], group.firstChild );
					/*tobePrint += "["
							+ level + ":" + group.firstChild + ","
							+ group.parent + "]";*/

					if ( this.previous != null )
					{
						saveToAggrValuesToDocument( level, rowId );
					}

				}
			}
			this.aggrHelper.onRow( this.getStartingGroup( ), this.getEndingGroup( ), this.current, rowId);

			if ( this.next == null )
			{
				for ( int i = 0; i < this.aggrOutput.length; i++ )
				{
					saveToAggrValuesToDocument( i, rowId );
				}
			}
			
			if ( this.runningAggrs.size( ) > 0 )
			{
				for ( String aggrName : this.runningAggrs )
				{
					IOUtil.writeObject( this.combinedAggrOutput,
							this.aggrHelper.getLatestAggrValue( aggrName ) );
				}
				
				IOUtil.writeLong( this.combinedAggrIndexOutput,
							this.combinedAggrRAOutput.getOffset( ) );
				
			}
		}
		catch ( IOException e )
		{
			throw new DataException( e.getLocalizedMessage( ), e );
		}
		/*if ( !tobePrint.isEmpty( ) )
			System.out.println( tobePrint );
		*/
		
	}

	private void saveToAggrValuesToDocument( int i , int rowId) throws IOException,
			DataException
	{
		if ( this.aggrOutput[i] != null )
		{
			for ( String aggrName : this.groupAggrs.get( i ) )
			{
				IOUtil.writeObject( this.aggrOutput[i],
						this.aggrHelper.getLatestAggrValue( aggrName ) );
			}
			if ( this.aggrIndexOutput[i] != null )
			{
				IOUtil.writeLong( this.aggrIndexOutput[i],
						this.aggrRAOutput[i].getOffset( ) );
			}
		}
		this.latestAggrAvailableIndex[i] = rowId;

	}

	/**
	 * Helper method to get the group break level between 2 rows
	 * 
	 * @param currRow
	 * @param prevRow
	 * @return
	 * @throws DataException
	 */
	private int getBreakLevel( IResultObject currRow, IResultObject prevRow) throws DataException
	{
		assert currRow != null;
		assert prevRow != null;

		int breakLevel = 0;
		for ( ; breakLevel < this.groupBys.length; breakLevel++ )
		{
			int colIndex = this.groupBys[breakLevel].getColumnIndex( );

			Object currObjectValue = null;
			Object prevObjectValue = null;
			if ( colIndex >= 0 )
			{
				currObjectValue = currRow.getFieldValue( colIndex );
				prevObjectValue = prevRow.getFieldValue( colIndex );
			}

			GroupBy groupBy = this.groupBys[breakLevel];
			if ( !groupBy.isInSameGroup( currObjectValue, prevObjectValue ) )
			{
				//current group is the break level
				//reset the groupBys of the inner groups within current group for the following compare
				for (int i = breakLevel + 1; i < this.groupBys.length; i++)
				{
					this.groupBys[i].reset( );
				}
				break;
			}
		}
		return breakLevel;
	}
	public void close( ) throws DataException
	{
		try
		{
			if ( this.groupOutput!= null  )
			{
				for( int i = 0; i < this.groupOutput.length; i++ )
				{
					this.groupOutput[i].seek( 0 );
					IOUtil.writeInt( this.groupOutput[i], this.groupInstanceIndex[i] );
					this.groupOutput[i].close( );
				}
				this.groupOutput = null;
			}
			if ( this.aggrOutput!= null )
			{
				for( int i = 0; i < this.aggrOutput.length; i++ )
				{
					if( this.aggrOutput[i]!= null )
						this.aggrOutput[i].close( );
				}
				this.aggrOutput = null;
			}
			if ( this.aggrIndexOutput!= null )
			{
				for( int i = 0; i < this.aggrIndexOutput.length; i++ )
				{
					if( this.aggrIndexOutput[i]!= null )
						this.aggrIndexOutput[i].close( );
				}
				this.aggrIndexOutput = null;
			}
			if( this.overallAggrs.size( ) > 0 && this.combinedAggrIndexOutput!= null )
			{
				this.combinedAggrIndexRAOutput.seek( 0 );
				IOUtil.writeLong( this.combinedAggrIndexOutput, this.combinedAggrRAOutput.getOffset( ) );
				this.combinedAggrIndexRAOutput.close( );
				for( String aggrName: overallAggrs )
				{
					IOUtil.writeObject( this.combinedAggrOutput, this.aggrHelper.getLatestAggrValue( aggrName ) );
				}
				this.combinedAggrOutput.close( );
			}
			if( this.aggrHelper!= null )
			{
				this.aggrHelper.close( );
				this.aggrHelper = null;
			}
			//TODO remove me
			if( this.groupBys.length == 0 )
			{
				OutputStream out = streamManager.getOutStream( DataEngineContext.GROUP_INFO_STREAM,
						StreamManager.ROOT_STREAM,
						StreamManager.SELF_SCOPE );
				IOUtil.writeInt(out, 0);
				out.close();
			}
		}
		catch ( IOException e )
		{
			throw new DataException( e.getLocalizedMessage( ), e );
		}
	}

	public void doSave( StreamManager manager ) throws DataException
	{
		try {
			this.streamManager = manager;
			if (this.streamManager != null) {
				this.groupOutput = new RAOutputStream[this.groupBys.length];
				this.aggrOutput = new DataOutputStream[this.groupBys.length];
				this.aggrRAOutput = new RAOutputStream[this.groupBys.length];
				this.aggrIndexOutput = new DataOutputStream[this.groupBys.length];

				if (this.overallAggrs.size() > 0
						|| this.runningAggrs.size() > 0) {
					this.combinedAggrIndexRAOutput = (RAOutputStream) streamManager
							.getOutStream(
									DataEngineContext.COMBINED_AGGR_INDEX_STREAM,
									StreamManager.ROOT_STREAM,
									StreamManager.BASE_SCOPE);
					this.combinedAggrRAOutput = (RAOutputStream) streamManager
							.getOutStream(
									DataEngineContext.COMBINED_AGGR_VALUE_STREAM,
									StreamManager.ROOT_STREAM,
									StreamManager.BASE_SCOPE);
					this.combinedAggrOutput = new DataOutputStream(
							this.combinedAggrRAOutput);
					this.combinedAggrIndexOutput = new DataOutputStream(
							this.combinedAggrIndexRAOutput);

					// write place holder for Overall aggregation index
					IOUtil.writeLong(this.combinedAggrIndexOutput, -1);

					// write the size of overall aggregation
					IOUtil.writeInt(this.combinedAggrOutput,
							this.overallAggrs.size());
					for (int i = 0; i < this.overallAggrs.size(); i++) {
						IOUtil.writeString(this.combinedAggrOutput,
								this.overallAggrs.get(i));
					}

					IOUtil.writeInt(this.combinedAggrOutput,
							this.runningAggrs.size());
					for (int i = 0; i < this.runningAggrs.size(); i++) {
						IOUtil.writeString(this.combinedAggrOutput,
								this.runningAggrs.get(i));
					}

					// write the starting index of first running aggregation
					IOUtil.writeLong(this.combinedAggrIndexOutput,
							this.combinedAggrRAOutput.getOffset());
				}

				for (int i = 0; i < this.groupBys.length; i++) {

					this.groupOutput[i] = (RAOutputStream) streamManager
							.getOutStream(
									DataEngineContext.PROGRESSIVE_VIEWING_GROUP_STREAM,
									i);

					IOUtil.writeInt(this.groupOutput[i], Integer.MAX_VALUE);
					if (!this.groupAggrs.get(i).isEmpty()) {
						this.aggrRAOutput[i] = streamManager.getOutStream(
								DataEngineContext.AGGR_VALUE_STREAM, i);
						this.aggrIndexOutput[i] = new DataOutputStream(
								streamManager.getOutStream(
										DataEngineContext.AGGR_INDEX_STREAM, i));
						this.aggrOutput[i] = new DataOutputStream(
								this.aggrRAOutput[i]);
						// The group level
						IOUtil.writeInt(this.aggrOutput[i], i + 1);
						// The number of aggregations in the group
						IOUtil.writeInt(this.aggrOutput[i], this.groupAggrs
								.get(i).size());
						for (String aggrName : this.groupAggrs.get(i)) {
							IOUtil.writeString(new DataOutputStream(
									this.aggrOutput[i]), aggrName);
						}
						IOUtil.writeLong(this.aggrIndexOutput[i],
								this.aggrRAOutput[i].getOffset());
					}

				}
			}
		} catch (IOException e) {
			throw new DataException(e.getLocalizedMessage(), e);
		}
	}
	
	public boolean isAggrAtIndexAvailable( String aggrName, int currentIndex ) throws DataException
	{
		assert this.aggrHelper!= null;
		return this.latestAggrAvailableIndex[this.aggrHelper.getAggrInfo( aggrName ).getGroupLevel( )-1] >= currentIndex;
		
	}
}
