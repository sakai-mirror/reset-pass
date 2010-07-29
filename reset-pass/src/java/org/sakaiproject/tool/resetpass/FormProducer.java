/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.resetpass;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.component.api.ServerConfigurationService;


import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;


/**
 * @author dhorwitz
 *
 */
public class FormProducer implements ViewComponentProducer, DefaultView,NavigationCaseReporter {

	public static final String VIEW_ID = "form";
	
	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ViewComponentProducer#getViewID()
	 */
	public String getViewID() {
		return this.VIEW_ID;
	}

	MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator ml) {
		messageLocator = ml;
	}
	
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService s) {
		this.serverConfigurationService = s;
	}
	
	private TargettedMessageList tml;
	  
	  public void setTargettedMessageList(TargettedMessageList tml) {
		    this.tml = tml;
	  }
	
	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewParms,
			ComponentChecker comp) {
		// TODO Auto-generated method stub

		
		if (tml!=null) {
			if (tml.size() > 0) {

		    	for (int i = 0; i < tml.size(); i ++ ) {
		    		UIBranchContainer errorRow = UIBranchContainer.make(tofill,"error-row:");
		    		if (tml.messageAt(i).args != null ) {	    		
		    			UIVerbatim.make(errorRow, "error", messageLocator.getMessage(tml.messageAt(i).acquireMessageCode(), (String[])tml.messageAt(i).args[0]));
		    		} else {
		    			UIVerbatim.make(errorRow, "error", messageLocator.getMessage(tml.messageAt(i).acquireMessageCode()));
		    		}
		    		
		    	}
		    }
		}
		
		
		
		String[] args = new String[1];
		args[0]=serverConfigurationService.getString("ui.service", "Sakai Bassed Service");
		UIVerbatim.make(tofill,"main",messageLocator.getMessage("mainText", args));
		UIForm form = UIForm.make(tofill,"form");
		UIInput.make(form,"input","#{userBean.email}");
		UICommand.make(form,"submit",UIMessage.make("postForm"),"#{formHandler.processAction}");
	}

	
	  public List<NavigationCase> reportNavigationCases() {
		    List<NavigationCase> togo = new ArrayList<NavigationCase>(); // Always navigate back to this view.
		    togo.add(new NavigationCase(null, new SimpleViewParameters(VIEW_ID)));
		    togo.add(new NavigationCase("Success", new SimpleViewParameters(ConfirmProducer.VIEW_ID)));
		    return togo;
	  }
	  
}
