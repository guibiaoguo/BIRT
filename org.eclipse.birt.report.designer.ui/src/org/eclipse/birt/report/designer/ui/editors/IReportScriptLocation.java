/*******************************************************************************
 * Copyright (c) 2007 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.designer.ui.editors;

/**
 * IReportScriptLocation
 */
public interface IReportScriptLocation
{

	/**
	 * @return Returns the full path of associated report file of current script
	 *         location
	 */
	String getReportFileName( );

	/**
	 * @return Returns the unique ID for current script location in associated
	 *         report
	 */
	String getID( );

	/**
	 * @return Returns the line number of current script location.
	 */
	int getLineNumber( );
	
	/**Gets the diplay name
	 * @return
	 */
	String getDisplayName();
}
