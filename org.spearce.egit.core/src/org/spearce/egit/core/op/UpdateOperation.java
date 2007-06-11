/*
 *  Copyright (C) 2007  Robin Rosenberg
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License, version 2.1, as published by the Free Software Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 */
package org.spearce.egit.core.op;

import java.util.Collection;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.spearce.egit.core.internal.UpdateJob;

/**
 * Updates the Git index for the selected resources. Only tracked resources
 * are updated.
 * <p>
 * Accepts a collection of resources (files and/or directories) whose content
 * should be updated in the corresponding Git repositories. Resources in the
 * collection can be associated with multiple repositories. 
 * </p>
 */
public class UpdateOperation implements IWorkspaceRunnable {
	private final Collection rsrcList;

	/**
	 * Create a new operation to update files/folders.
	 * 
	 * @param rsrcs
	 *            collection of {@link IResource}s which should be added to the
	 *            relevant Git repositories.
	 */
	public UpdateOperation(final Collection rsrcs) {
		rsrcList = rsrcs;
	}

	public void run(IProgressMonitor m) throws CoreException {
		new UpdateJob(rsrcList).schedule();
	}
}
