/*******************************************************************************
 * Copyright (c) 2004 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.designer.ui.preferences;

import org.eclipse.birt.report.designer.nls.Messages;
import org.eclipse.birt.report.designer.ui.ReportPlugin;
import org.eclipse.birt.report.designer.ui.util.PixelConverter;
import org.eclipse.birt.report.designer.ui.views.attributes.providers.ChoiceSetFactory;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.elements.ReportDesignConstants;
import org.eclipse.birt.report.model.api.metadata.IChoice;
import org.eclipse.birt.report.model.api.metadata.IChoiceSet;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 */
public class LayoutConfigurationBlock extends OptionsConfigurationBlock
{

	private static final String AUTO = Messages.getString("LayoutConfigurationBlock.Default.Unit.Auto"); //$NON-NLS-1$

	private final Key PREF_DEFAULT_UNIT = getReportKey( ReportPlugin.DEFAULT_UNIT_PREFERENCE );

	private final static String DEFAULT_UNIT = Messages.getString("LayoutConfigurationBlock.Default.Unit.Label"); //$NON-NLS-1$

	public final int LTR_DIRECTION_INDX = 0;
	public final int RTL_DIRECTION_INDX = 1;

	private PixelConverter fPixelConverter;

	public LayoutConfigurationBlock( IStatusChangeListener context,
			IProject project )
	{
		super( context, ReportPlugin.getDefault( ), project );
		setKeys( getKeys( ) );
	}

	private Key[] getKeys( )
	{
		Key[] keys = new Key[]{
			PREF_DEFAULT_UNIT
		};
		return keys;
	}

	/*
	 * @see
	 * org.eclipse.jface.preference.PreferencePage#createContents(Composite)
	 */
	protected Control createContents( Composite parent )
	{
		fPixelConverter = new PixelConverter( parent );
		setShell( parent.getShell( ) );

		Composite mainComp = new Composite( parent, SWT.NONE );
		mainComp.setFont( parent.getFont( ) );
		GridLayout layout = new GridLayout( );
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		mainComp.setLayout( layout );

		Composite othersComposite = createBuildPathTabContent( mainComp );
		GridData gridData = new GridData( GridData.FILL,
				GridData.FILL,
				true,
				true );
		gridData.heightHint = fPixelConverter.convertHeightInCharsToPixels( 20 );
		othersComposite.setLayoutData( gridData );

		validateSettings( null, null, null );

		return mainComp;
	}

	private Composite createBuildPathTabContent( Composite parent )
	{
		IChoiceSet choiceSet = ChoiceSetFactory.getElementChoiceSet( ReportDesignConstants.REPORT_DESIGN_ELEMENT,
				ReportDesignHandle.UNITS_PROP );

		int len = choiceSet.getChoices( ).length;

		String[] unitValues = new String[len + 1];
		String[] unitNames = new String[len + 1];

		unitNames[0] = AUTO;
		unitValues[0] = ReportPlugin.DEFAULT_UNIT_AUTO;

		for ( int i = 0; i < len; i++ )
		{
			IChoice ch = choiceSet.getChoices( )[i];
			unitValues[i + 1] = ch.getName( );
			unitNames[i + 1] = ch.getDisplayName( );
		}

		Composite pageContent = new Composite( parent, SWT.NONE );

		GridData data = new GridData( GridData.FILL_HORIZONTAL
				| GridData.FILL_VERTICAL
				| GridData.VERTICAL_ALIGN_BEGINNING );
		data.grabExcessHorizontalSpace = true;
		pageContent.setLayoutData( data );

		GridLayout layout = new GridLayout( );
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 3;
		pageContent.setLayout( layout );

		addComboBox( pageContent,
				DEFAULT_UNIT,
				PREF_DEFAULT_UNIT,
				unitValues,
				unitNames,
				0 );

		return pageContent;
	}

	/*
	 * (non-javadoc) Update fields and validate. @param changedKey Key that
	 * changed, or null, if all changed.
	 */

	public void performDefaults( )
	{
		super.performDefaults( );
	}

	public void useProjectSpecificSettings( boolean enable )
	{
		super.useProjectSpecificSettings( enable );
	}
}
