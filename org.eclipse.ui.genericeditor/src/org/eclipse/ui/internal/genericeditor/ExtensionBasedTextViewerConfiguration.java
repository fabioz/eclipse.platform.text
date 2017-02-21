/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Sopot Cela, Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.ui.internal.genericeditor;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioningListener;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;

import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

/**
 * The configuration of the {@link ExtensionBasedTextEditor}. It registers the proxy composite
 * for hover, completion, syntax highlighting, and then those proxy take care of resolving to
 * the right extensions on-demand.
 * 
 * @since 1.0
 */
public final class ExtensionBasedTextViewerConfiguration extends TextSourceViewerConfiguration implements IDocumentPartitioningListener {

	private ITextEditor editor;
	private Set<IContentType> contentTypes;
	private IDocument document;

	private ContentAssistant contentAssistant;
	private List<IContentAssistProcessor> processors;

	/**
	 * 
	 * @param editor the editor we're creating.
	 * @param preferenceStore the preference store.
	 */
	public ExtensionBasedTextViewerConfiguration(ITextEditor editor, IPreferenceStore preferenceStore) {
		super(preferenceStore);
		this.editor = editor;
		this.editor.addPropertyListener(new IPropertyListener() {
			@Override
			public void propertyChanged(Object source, int propId) {
				if (propId == IEditorPart.PROP_INPUT) {
					watchDocument(editor.getDocumentProvider().getDocument(editor.getEditorInput()));
				}
			}
		});
	}

	private Set<IContentType> getContentTypes() {
		if (this.contentTypes == null) {
			this.contentTypes = new LinkedHashSet<>();
			Queue<IContentType> types = new LinkedList<>(Arrays.asList(Platform.getContentTypeManager().findContentTypesFor(editor.getEditorInput().getName())));
			while (!types.isEmpty()) {
				IContentType type = types.poll();
				this.contentTypes.add(type);
				IContentType parent = type.getBaseType();
				if (parent != null) {
					types.add(parent);
				}
			}
		}
		return this.contentTypes;
	}

	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		TextHoverRegistry registry= GenericEditorPlugin.getDefault().getHoverRegistry();
		return registry.getAvailableHover(sourceViewer, getContentTypes());
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		ContentAssistProcessorRegistry registry= GenericEditorPlugin.getDefault().getContentAssistProcessorRegistry();
		contentAssistant= new ContentAssistant(true);
		contentAssistant.setContextInformationPopupOrientation(ContentAssistant.CONTEXT_INFO_BELOW);
		contentAssistant.setProposalPopupOrientation(ContentAssistant.PROPOSAL_REMOVE);
		contentAssistant.enableColoredLabels(true);
		contentAssistant.enableAutoActivation(true);
		this.processors = registry.getContentAssistProcessors(sourceViewer, getContentTypes());
		for (IContentAssistProcessor processor : this.processors) {
			contentAssistant.addContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
		}
		if (this.document != null) {
			associateTokenContentTypes(this.document);
		}
		contentAssistant.setInformationControlCreator(new AbstractReusableInformationControlCreator() {
			@Override
			protected IInformationControl doCreateInformationControl(Shell parent) {
				return new DefaultInformationControl(parent);
			}
		});
		return contentAssistant;
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconcilerRegistry registry = GenericEditorPlugin.getDefault().getPresentationReconcilerRegistry();
		List<IPresentationReconciler> reconciliers = registry.getPresentationReconcilers(sourceViewer, getContentTypes());
		if (!reconciliers.isEmpty()) {
			return reconciliers.get(0);
		}
		return super.getPresentationReconciler(sourceViewer);
	}

	void watchDocument(IDocument document) {
		if (this.document == document) {
			return;
		}
		if (this.document != null) {
			this.document.removeDocumentPartitioningListener(this);
		}
		this.document = document;
		associateTokenContentTypes(document);
		document.addDocumentPartitioningListener(this);
	}

	@Override
	public void documentPartitioningChanged(IDocument document) {
		associateTokenContentTypes(document);
	}

	private void associateTokenContentTypes(IDocument document) {
		if (contentAssistant == null || this.processors == null) {
			return;
		}
		for (String legalTokenContentType : document.getLegalContentTypes()) {
			for (IContentAssistProcessor processor : this.processors) {
				contentAssistant.addContentAssistProcessor(processor, legalTokenContentType);
			}
		}
	}

}
