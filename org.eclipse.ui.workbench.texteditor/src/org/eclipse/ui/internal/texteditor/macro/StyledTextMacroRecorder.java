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

import org.eclipse.e4.core.macros.EMacroContext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * A listener that will record actions done in a StyledText and add them to the
 * macro.
 * 
 * @since 3.11
 */
public class StyledTextMacroRecorder implements Listener {

	private final EMacroContext fMacroContext;

	public StyledTextMacroRecorder(EMacroContext macroContext) {
		this.fMacroContext = macroContext;
	}

	@Override
	public void handleEvent(Event event) {
		if (event.type == SWT.KeyDown && fMacroContext.isRecording()) {
			fMacroContext.addCommand(new StyledTextKeyDownMacroCommand(event));
		}
	}

	public void uninstall(StyledText textWidget) {
		textWidget.removeListener(SWT.KeyDown, this);
	}

	public void install(StyledText textWidget) {
		textWidget.addListener(SWT.KeyDown, this);
	}
}
