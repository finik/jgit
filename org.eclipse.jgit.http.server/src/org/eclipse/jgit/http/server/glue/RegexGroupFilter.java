/*
 * Copyright (C) 2009, Google Inc.
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

package org.eclipse.jgit.http.server.glue;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Switch servlet path and path info to use another regex match group.
 * <p>
 * This filter is meant to be installed in the middle of a pipeline created by
 * {@link MetaServlet#serveRegex(String)}. The passed request's servlet path is
 * updated to be all text up to the start of the designated capture group, and
 * the path info is changed to the contents of the capture group.
 **/
public class RegexGroupFilter implements Filter {
	private final int groupIdx;

	/**
	 * @param groupIdx
	 *            capture group number, 1 through the number of groups.
	 */
	public RegexGroupFilter(final int groupIdx) {
		if (groupIdx < 1)
			throw new IllegalArgumentException("Invalid index: " + groupIdx);
		this.groupIdx = groupIdx - 1;
	}

	public void init(FilterConfig config) throws ServletException {
		// Do nothing.
	}

	public void destroy() {
		// Do nothing.
	}

	public void doFilter(final ServletRequest request,
			final ServletResponse rsp, final FilterChain chain)
			throws IOException, ServletException {
		final WrappedRequest[] g = groupsFor(request);
		if (groupIdx < g.length)
			chain.doFilter(g[groupIdx], rsp);
		else
			throw new ServletException("Invalid regex group " + (groupIdx + 1));
	}

	private static WrappedRequest[] groupsFor(final ServletRequest r) {
		return (WrappedRequest[]) r.getAttribute(MetaServlet.REGEX_GROUPS);
	}
}