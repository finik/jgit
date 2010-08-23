/*
 * Copyright (C) 2010, Marc Strapetz <marc.strapetz@syntevo.com>
 * and other copyright owners as documented in the project's IP log.
 *
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Distribution License v1.0 which
 * accompanies this distribution, is reproduced below, and is
 * available at http://www.eclipse.org/org/documents/edl-v10.php
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name of the Eclipse Foundation, Inc. nor the
 *   names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.eclipse.jgit.treewalk;

import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.CoreConfig;

/**
 * Contains options used by the WorkingTreeIterator.
 */
public class WorkingTreeOptions {

	/**
	 * Creates default options which reflect the original configuration of Git
	 * on Unix systems.
	 *
	 * @return created working tree options
	 */
	public static WorkingTreeOptions createDefaultInstance() {
		return new WorkingTreeOptions(false);
	}

	/**
	 * Creates options based on the specified repository configuration.
	 *
	 * @param config
	 *            repository configuration to create options for
	 *
	 * @return created working tree options
	 */
	public static WorkingTreeOptions createConfigurationInstance(Config config) {
		return new WorkingTreeOptions(config.get(CoreConfig.KEY).isAutoCRLF());
	}

	/**
	 * Indicates whether EOLs of text files should be converted to '\n' before
	 * calculating the blob ID.
	 **/
	private final boolean autoCRLF;

	/**
	 * Creates new options.
	 *
	 * @param autoCRLF
	 *            indicates whether EOLs of text files should be converted to
	 *            '\n' before calculating the blob ID.
	 */
	public WorkingTreeOptions(boolean autoCRLF) {
		this.autoCRLF = autoCRLF;
	}

	/**
	 * Indicates whether EOLs of text files should be converted to '\n' before
	 * calculating the blob ID.
	 *
	 * @return true if EOLs should be canonicalized.
	 */
	public boolean isAutoCRLF() {
		return autoCRLF;
	}
}