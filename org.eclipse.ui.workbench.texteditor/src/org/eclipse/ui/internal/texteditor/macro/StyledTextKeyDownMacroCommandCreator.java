/*******************************************************************************
 * Copyright (c) 2017 Fabio Zadrozny and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabio Zadrozny - initial API and implementation - https://bugs.eclipse.org/bugs/show_bug.cgi?id=8519
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.macro;

import java.util.Map;

import org.eclipse.e4.core.macros.IMacroCommand;
import org.eclipse.e4.core.macros.IMacroCreator;

/**
 * @since 3.11
 */
public class StyledTextKeyDownMacroCommandCreator implements IMacroCreator {

	@Override
	public IMacroCommand create(Map<String, String> stringMap) {
		return StyledTextKeyDownMacroCommand.fromMap(stringMap);
	}

}
