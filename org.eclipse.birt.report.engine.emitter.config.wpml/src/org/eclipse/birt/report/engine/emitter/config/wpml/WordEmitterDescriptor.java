/*******************************************************************************
 * Copyright (c) 2008 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.engine.emitter.config.wpml;

import java.util.Locale;

import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.eclipse.birt.report.engine.emitter.config.AbstractConfigurableOptionObserver;
import org.eclipse.birt.report.engine.emitter.config.AbstractEmitterDescriptor;
import org.eclipse.birt.report.engine.emitter.config.ConfigurableOption;
import org.eclipse.birt.report.engine.emitter.config.IConfigurableOption;
import org.eclipse.birt.report.engine.emitter.config.IConfigurableOptionObserver;
import org.eclipse.birt.report.engine.emitter.config.IOptionValue;
import org.eclipse.birt.report.engine.emitter.config.wpml.i18n.Messages;

/**
 * This class is a descriptor of word emitter.
 */
public class WordEmitterDescriptor extends AbstractEmitterDescriptor
{
	protected static final String CHART_DPI = "ChartDpi";
	
	protected IConfigurableOption[] options;
	private Locale locale;
	
	public WordEmitterDescriptor( )
	{
		initOptions( );
	}

	public void setLocale( Locale locale )
	{
		if ( this.locale != locale )
		{
			this.locale = locale;
			initOptions( );
		}
	}

	private void initOptions( )
	{
		// Initializes the option for chart DPI.
		ConfigurableOption chartDpi = new ConfigurableOption( CHART_DPI );
		chartDpi.setDisplayName( getMessage( "OptionDisplayValue.ChartDpi" ) ); //$NON-NLS-1$
		chartDpi.setDataType( IConfigurableOption.DataType.INTEGER );
		chartDpi.setDisplayType( IConfigurableOption.DisplayType.TEXT );
		chartDpi.setDefaultValue( new Integer( 192 ) );
		chartDpi.setToolTip( getMessage( "Tooltip.ChartDpi" ) );
		chartDpi.setDescription( getMessage( "OptionDescription.ChartDpi" ) ); //$NON-NLS-1$

		options = new IConfigurableOption[]{chartDpi};
	}

	private String getMessage( String key )
	{
		return Messages.getString( key, locale );
	}

	@Override
	public IConfigurableOptionObserver createOptionObserver( )
	{
		return new WordOptionObserver( );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.birt.report.engine.emitter.config.IEmitterDescriptor#
	 * getDescription()
	 */
	public String getDescription( )
	{
		return getMessage( "WordEmitter.Description" ); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.birt.report.engine.emitter.config.IEmitterDescriptor#
	 * getDisplayName()
	 */
	public String getDisplayName( )
	{
		return getMessage( "WordEmitter.DisplayName" ); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.birt.report.engine.emitter.config.IEmitterDescriptor#getID()
	 */
	public String getID( )
	{
		return "org.eclipse.birt.report.engine.emitter.word"; //$NON-NLS-1$
	}
	
	public String getRenderOptionName( String name )
	{
		assert name != null;
		if ( CHART_DPI.equals( name ) )
		{
			return IRenderOption.CHART_DPI;
		}
		return name;
	}

	class WordOptionObserver extends AbstractConfigurableOptionObserver
	{

		@Override
		public IConfigurableOption[] getOptions( )
		{
			return options;
		}

		@Override
		public IRenderOption getPreferredRenderOption( )
		{
			RenderOption renderOption = new RenderOption( );

			renderOption.setEmitterID( getID( ) );
			renderOption.setOutputFormat( "doc" ); //$NON-NLS-1$

			if ( values != null && values.length > 0 )
			{
				for ( IOptionValue optionValue : values )
				{
					if ( optionValue != null )
					{
						renderOption.setOption(
								getRenderOptionName( optionValue.getName( ) ),
								optionValue.getValue( ) );
					}
				}
			}

			return renderOption;
		}
	}

}
