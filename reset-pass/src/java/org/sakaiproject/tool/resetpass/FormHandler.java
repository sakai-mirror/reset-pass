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
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.email.api.EmailService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;

import uk.org.ponder.messageutil.MessageLocator;

public class FormHandler {
	
	private static java.lang.String SECURE_UPDATE_USER_ANY = org.sakaiproject.user.api.UserDirectoryService.SECURE_UPDATE_USER_ANY;

	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator messageLocator) {
		  
	    this.messageLocator = messageLocator;
	  }
	
	
	private UserDirectoryService userDirectoryService;
	public void setUserDirectoryService(UserDirectoryService ds){
		this.userDirectoryService = ds;
	}
	
	
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService s) {
		this.serverConfigurationService = s;
	}
	
	private EmailService emailService;
	public void setEmailService(EmailService e) {
		this.emailService = e;
	}
	
	private RetUser userBean;
	public void setUserBean(RetUser u){
		this.userBean = u;
	}
	
	private EventTrackingService  eventService;
	public void setEventService(EventTrackingService  etc) {
		eventService=etc;
	}
	
	private SecurityService securityService;
	public void setSecurityService(SecurityService ss) {
		securityService = ss;
	}
	
	private static Log m_log  = LogFactory.getLog(FormHandler.class);
	
	public String processAction() {
		m_log.info("getting password for " + userBean.getEmail());
			
			String from = serverConfigurationService.getString("setup.request", null);
			if (from == null)
			{
				m_log.warn(this + " - no 'setup.request' in configuration");
				from = "postmaster@".concat(serverConfigurationService.getServerName());
			}
			
			//now we need to reset the password
			try {

			// Need: SECURE_UPDATE_USER_ANY
			securityService.pushAdvisor(new SecurityAdvisor() {
			    public SecurityAdvice isAllowed(String userId, String function, String reference) {
			            if (SECURE_UPDATE_USER_ANY.equals(function)) {
			                return SecurityAdvice.ALLOWED;
			             }
			            return SecurityAdvice.PASS;
			         }
			    });
			            
			UserEdit userE = userDirectoryService.editUser(userBean.getUser().getId().trim());
			String pass = getRandPass();
			userE.setPassword(pass);
			userDirectoryService.commitEdit(userE);

			securityService.popAdvisor();
			
			String productionSiteName = serverConfigurationService.getString("ui.service", "");
			
			StringBuffer buff = new StringBuffer();
			buff.setLength(0);
			buff.append(messageLocator.getMessage("mailBodyPre",userE.getDisplayName()) + "\n\n");
			
			buff.append(messageLocator.getMessage("mailBody1",new Object[]{productionSiteName, serverConfigurationService.getPortalUrl()})+ "\n\n");
			buff.append(messageLocator.getMessage("mailBody2",new Object[]{userE.getEid()})+ "\n");
			buff.append(messageLocator.getMessage("mailBody3",new Object[]{pass})+ "\n\n");
			
			if (serverConfigurationService.getString("support.email", null) != null )
				buff.append(messageLocator.getMessage("mailBody4",new Object[]{serverConfigurationService.getString("support.email")}) + "\n\n");
			
			m_log.debug(messageLocator.getMessage("mailBody1",new Object[]{productionSiteName}));
			buff.append(messageLocator.getMessage("mailBodySalut")+"\n");
			buff.append(messageLocator.getMessage("mailBodySalut1",productionSiteName));
			
			String body = buff.toString();
			m_log.debug("body: " + body);
			
			List headers = new ArrayList();
			headers.add("Precedence: bulk");
			
			emailService.send(from,userBean.getUser().getEmail(),messageLocator.getMessage("mailSubject", new Object[]{productionSiteName}),body,
					userBean.getUser().getEmail(), null, headers);
          
			m_log.info("New password emailed to: " + userE.getEid() + " (" + userE.getId() + ")");
			eventService.post(eventService.newEvent("user.resetpass", userE.getReference() , true));
			
			}
			catch (Exception e) {
				e.printStackTrace();
				securityService.popAdvisor();
				return null;
			}
		
		return "Success";
	}
	
	//borrowed from siteaction
	private String getRandPass() {
		// set password to a random positive number
		Random generator = new Random(System.currentTimeMillis());
		Integer num = new Integer(generator.nextInt(Integer.MAX_VALUE));
		if (num.intValue() < 0) num = new Integer(num.intValue() *-1);
		return num.toString();
	}
}
