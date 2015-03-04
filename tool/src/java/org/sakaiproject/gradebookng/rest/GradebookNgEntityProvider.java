package org.sakaiproject.gradebookng.rest;

import java.util.List;
import java.util.Map;

import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.StringUtils;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.Permissions;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;

/**
 * This entity provider is to support some of the Javascript front end pieces.
 * It never was built to support third party access, and never will support that
 * use case.
 * 
 * The data you need for Gradebook integrations should already be available in
 * the standard gradebook entityprovider
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 * 
 */
@CommonsLog
public class GradebookNgEntityProvider extends AbstractEntityProvider implements
		EntityProvider, AutoRegisterEntityProvider, ActionsExecutable,
		Outputable, Describeable {

	@Override
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.JSON };
	}

	@Override
	public String getEntityPrefix() {
		return "gbng";
	}

	/**
	 * site/assignment-list
	 * @throws IdUnusedException 
	 */
	@EntityCustomAction(action = "assignments", viewKey = EntityView.VIEW_LIST)
	public List<Assignment> getAssignmentList(EntityView view) {

		// get siteId
		String siteId = view.getPathSegment(2);

		// check siteId supplied
		if (StringUtils.isBlank(siteId)) {
			throw new IllegalArgumentException(
					"Site ID must be set in order to access GBNG data.");
		}
		checkValidSite(siteId);

		// check instructor
		checkInstructor(siteId);
		
		// get assignment list
		List<Assignment> assignments = this.businessService.getGradebookAssignments(siteId);
				
		return assignments;
	}
	
	/**
	 * Update the order of an assignment in the gradebook
	 * This is a per site setting.
	 * 
	 * @param ref
	 * @param params map, must include:
	 * siteId
	 * assignmentId
	 * new order
	 * 
	 * an assignmentorder object will be created and saved as a list in the XML property 'gbng_assignment_order'
	 */
	@EntityCustomAction(action = "assignment-order", viewKey = EntityView.VIEW_NEW)
	public void updateAssignmentOrder(EntityReference ref, Map<String, Object> params) {
		
		// get params
		String siteId = (String) params.get("siteId");
		String assignmentId = (String) params.get("assignmentId");
		String order = (String) params.get("order");

		// check params supplied are valid
		if (StringUtils.isBlank(siteId) || StringUtils.isBlank(assignmentId) || StringUtils.isBlank(order)) {
			throw new IllegalArgumentException(
					"Data was missing from the request");
		}
		checkValidSite(siteId);

		// check instructor
		checkInstructor(siteId);
		
		//TODO get the current prop, update it and resave
		
		
	}
	/**
	 * Helper to check if the user is an instructor. Throws IllegalArgumentException if not.
	 * We don't currently need the value that this produces so we don't return it.
	 * 
	 * @param siteId
	 * @return
	 * @throws IdUnusedException
	 */
	private void checkInstructor(String siteId) {
		
		String currentUserId = this.getCurrentUserId();
		
		if(StringUtils.isBlank(currentUserId)) {
			throw new SecurityException("You must be logged in to access GBNG data");
		}
		
		if(!isAllowed(currentUserId, Permissions.GRADE_ALL.getValue(), siteService.siteReference(siteId))) {
			throw new SecurityException("You do not have instructor-type permissions in this site.");
		}
	}

	/**
	 * Helper to get current user id
	 * 
	 * @return
	 */
	private String getCurrentUserId() {
		return sessionManager.getCurrentSessionUserId();
	}

	/**
	 * Helper to check user is allowed
	 * 
	 * @param userId
	 * @param permission
	 * @param locationId
	 * @return
	 */
	private boolean isAllowed(String userId, String permission, String locationId) {
		if (securityService.unlock(userId, permission, locationId)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Helper to check a site ID is valid. Throws IllegalArgumentException if not.
	 * We don't currently need the site that this produces so we don't return it.
	 * @param siteId
	 */
	@SuppressWarnings("unused")
	private void checkValidSite(String siteId) {
		try {
			Site site = this.siteService.getSite(siteId);
		} catch (IdUnusedException e) {
			throw new IllegalArgumentException("Invalid site id");
		}
	}

	@Setter
	private SiteService siteService;

	@Setter
	private SessionManager sessionManager;

	@Setter
	private SecurityService securityService;
	
	@Setter
	private GradebookNgBusinessService businessService;
	
}
