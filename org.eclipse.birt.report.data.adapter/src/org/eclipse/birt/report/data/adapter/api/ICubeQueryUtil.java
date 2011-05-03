
/*******************************************************************************
 * Copyright (c) 2004, 2005 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/
package org.eclipse.birt.report.data.adapter.api;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.birt.data.engine.api.IBinding;
import org.eclipse.birt.data.engine.core.DataException;
import org.eclipse.birt.data.engine.olap.api.query.ICubeQueryDefinition;
import org.eclipse.birt.data.engine.olap.api.query.ILevelDefinition;
import org.eclipse.birt.report.model.api.olap.CubeHandle;
import org.eclipse.birt.report.model.api.olap.TabularCubeHandle;

/**
 * 
 */

public interface ICubeQueryUtil
{
	public static String DISPLAY_NAME_ATTR = "DisplayName";

	/**
	 * Return all the level/level attributes that referenced by an expression.
	 * 
	 * @param expression
	 * @return
	 * @throws AdapterException
	 */
	public IDimensionLevel[] getReferencedDimensionLevel( String expression ) throws AdapterException;
	
	/**
	 * Return all the level/level attributes that referenced by an expression,
	 * including the referenced binding
	 * 
	 * @param expression
	 * @param bindings
	 * @return
	 * @throws AdapterException
	 */
	public IDimensionLevel[] getReferencedDimensionLevel( String expression,
			List<IBinding> bindings ) throws AdapterException;

	/**
	 * Utility method to acquire referable bindings, either in cube filter or
	 * cube sort.
	 * 
	 * @param targetLevel
	 * @param bindings
	 * @param isSort
	 * @return
	 * @throws AdapterException
	 */
	public List getReferableBindings( String targetLevel,
			ICubeQueryDefinition cubeQueryDefn, boolean isSort ) throws AdapterException;
	
	/**
	 * Utility method to acquire referable measure bindings.
	 * @param measureName
	 * @param cubeDefn
	 * @return
	 * @throws DataException
	 */
	public List getReferableMeasureBindings( String measureName,
			ICubeQueryDefinition cubeDefn ) throws DataException;
	/**
	 * Return a list of ILevelDefinition instances that referenced by 
	 * 
	 * @param targetLevel
	 * @param bindingExpr
	 * @param queryDefn
	 * @return
	 * @throws AdapterException
	 */
	public List getReferencedLevels( String targetLevel,
			String bindingExpr, ICubeQueryDefinition queryDefn ) throws AdapterException;

	/**
	 * Return the measure name referenced by the expression.
	 * 
	 * @param expr
	 * @return
	 * @throws AdapterException
	 */
	public String getReferencedMeasureName( String expr ) throws AdapterException;
	
	/**
	 * Return cascading member values.
	 * 
	 * @deprecated use method with signature
	 *             <code>getMemberValueIterator( TabularCubeHandle cubeHandle,
			String targetLevel, DimensionLevel[] higherLevelDefns,
			Object[] values ) throws AdapterException</code>
	 *             instead.
	 * @param cubeHandle
	 * @param targetLevel
	 * @param higherLevelDefns
	 * @param values
	 * @return
	 * @throws AdapterException
	 */
	public Iterator getMemberValueIterator( TabularCubeHandle cubeHandle,
			String targetLevel, ILevelDefinition[] higherLevelDefns,
			Object[] values ) throws AdapterException;
	
	/**
	 * @deprecated use method with signature
	 *             <code>getMemberValueIterator( TabularCubeHandle cubeHandle,
			String targetLevel, DimensionLevel[] higherLevelDefns,
			Object[] values, Map appContext ) throws AdapterException</code>
	 *             instead.
	 * @param cubeHandle
	 * @param targetLevel
	 * @param higherLevelDefns
	 * @param values
	 * @param appContext
	 * @return
	 * @throws AdapterException
	 */
	public Iterator getMemberValueIterator( TabularCubeHandle cubeHandle,
			String targetLevel, ILevelDefinition[] higherLevelDefns,
			Object[] values, Map appContext ) throws AdapterException;
	
	/**
	 * 
	 * @param cubeHandle
	 * @param targetLevel
	 * @param dimensionLevels
	 * @param values
	 * @return
	 * @deprecated use Method getMemberValueIterator( CubeHandle cubeHandle,
			String targetLevel, DimensionLevel[] dimensionLevels,
			Object[] values, Map appContext ) throws AdapterException;
	 * @throws AdapterException
	 */
	public Iterator getMemberValueIterator( TabularCubeHandle cubeHandle,
			String targetLevel, DimensionLevel[] dimensionLevels,
			Object[] values ) throws AdapterException;
	
	/**
	 * 
	 * @param cubeHandle
	 * @param targetLevel
	 * @param dimensionLevels
	 * @param values
	 * @return
	 * @throws AdapterException
	 */
	public Iterator getMemberValueIterator( CubeHandle cubeHandle,
			String targetLevel, DimensionLevel[] dimensionLevels,
			Object[] values ) throws AdapterException;
	/**
	 * 
	 * @param cubeHandle
	 * @param targetLevel
	 * @param dimensionLevels
	 * @param values
	 * @param appContext
	 * @return
	 * @deprecated use Method getMemberValueIterator( CubeHandle cubeHandle,
			String targetLevel, DimensionLevel[] dimensionLevels,
			Object[] values, Map appContext ) throws AdapterException;
	 * @throws AdapterException
	 */
	public Iterator getMemberValueIterator( TabularCubeHandle cubeHandle,
			String targetLevel, DimensionLevel[] dimensionLevels,
			Object[] values, Map appContext ) throws AdapterException;

	/**
	 * 
	 * @param cubeHandle
	 * @param targetLevel
	 * @param dimensionLevels
	 * @param values
	 * @param appContext
	 * @return
	 * @throws AdapterException
	 */
	public Iterator getMemberValueIterator( CubeHandle cubeHandle,
			String targetLevel, DimensionLevel[] dimensionLevels,
			Object[] values, Map appContext ) throws AdapterException;

	/**
	 * Return member value of a given level.
	 * 
	 * @param cubeHandle
	 * @param dataBindingExpr
	 * @param queryDefn
	 * @return
	 * @deprecated use Method getMemberValueIterator( CubeHandle cubeHandle,
	 *             String dataBindingExpr, ICubeQueryDefinition queryDefn ) throws AdapterException;
	 * @throws AdapterException
	 */
	public Iterator getMemberValueIterator( TabularCubeHandle cubeHandle,
			String dataBindingExpr, ICubeQueryDefinition queryDefn )
			throws AdapterException;
	
	/**
	 * Return member value of a given level.
	 * 
	 * @param cubeHandle
	 * @param dataBindingExpr
	 * @param queryDefn
	 * @return
	 * @throws AdapterException
	 */
	public Iterator getMemberValueIterator( CubeHandle cubeHandle,
			String dataBindingExpr, ICubeQueryDefinition queryDefn )
			throws AdapterException;
	
	/**
	 * 
	 * @param cubeHandle
	 * @param dataBindingExpr
	 * @param queryDefn
	 * @param appContext
	 * @return
	 * @deprecated use Method getMemberValueIterator( CubeHandle cubeHandle,
	 *             String dataBindingExpr, ICubeQueryDefinition queryDefn,Map
	 *             appContext ) throws AdapterException;
	 * @throws AdapterException
	 */
	public Iterator getMemberValueIterator( TabularCubeHandle cubeHandle,
			String dataBindingExpr, ICubeQueryDefinition queryDefn,
			Map appContext ) throws AdapterException;
	
	
	/**
	 * 
	 * @param cubeHandle
	 * @param dataBindingExpr
	 * @param queryDefn
	 * @param appContext
	 * @return
	 * @throws AdapterException
	 */
	public Iterator getMemberValueIterator( CubeHandle cubeHandle,
			String dataBindingExpr, ICubeQueryDefinition queryDefn,Map appContext )
			throws AdapterException;
	
	/**
	 * This is a utility method for GUI to find out all the invalid bindings in
	 * a xTab definition.
	 * 
	 * @param queryDefn
	 * @return
	 * @throws AdapterException
	 */
	public List getInvalidBindings( ICubeQueryDefinition queryDefn )
			throws AdapterException;	
	
	/**
	 * Checks whether the given string is valid to be the name for a level/dimension 
	 * 
	 * @param name
	 * @return
	 */
	public boolean isValidDimensionName( String name );


}