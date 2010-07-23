/**
 * Copyright 2008 Anders Hessellund 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id: AnalysisException.java,v 1.1 2008/01/17 18:48:18 hessellund Exp $
 */
package dk.itu.smartemf.ofbiz.analysis;
@SuppressWarnings("serial")
public class AnalysisException extends RuntimeException { 
	public AnalysisException(String msg) { 
		super(msg);
	}
	public AnalysisException(String msg, Throwable exception) {
		super(msg,exception);
	}
}