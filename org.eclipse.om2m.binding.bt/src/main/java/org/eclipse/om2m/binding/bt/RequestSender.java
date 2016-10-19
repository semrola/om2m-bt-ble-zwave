package org.eclipse.om2m.binding.bt;

import java.math.BigInteger;

import org.eclipse.om2m.commons.constants.Constants;
import org.eclipse.om2m.commons.constants.MimeMediaType;
import org.eclipse.om2m.commons.constants.Operation;
import org.eclipse.om2m.commons.constants.ResourceType;
import org.eclipse.om2m.commons.resource.AE;
import org.eclipse.om2m.commons.resource.Container;
import org.eclipse.om2m.commons.resource.ContentInstance;
import org.eclipse.om2m.commons.resource.RequestPrimitive;
import org.eclipse.om2m.commons.resource.Resource;
import org.eclipse.om2m.commons.resource.ResponsePrimitive;
import org.eclipse.om2m.core.service.CseService;

public class RequestSender {
	public static CseService CSE;
	
	private RequestSender(){}
	
	public static ResponsePrimitive createResource(String targetId, String name, Resource resource, int resourceType){
		RequestPrimitive request = new RequestPrimitive();
		request.setFrom(Constants.ADMIN_REQUESTING_ENTITY);
		request.setTargetId(targetId);
		request.setResourceType(BigInteger.valueOf(resourceType));
		request.setRequestContentType(MimeMediaType.OBJ);
		request.setReturnContentType(MimeMediaType.OBJ);
		request.setContent(resource);
		request.setName(name);
		request.setOperation(Operation.CREATE);
		return CSE.doRequest(request);
	}
	
	public static ResponsePrimitive checkExistance(String targetId) {
		RequestPrimitive request = new RequestPrimitive();
		request.setFrom(Constants.ADMIN_REQUESTING_ENTITY);
		request.setTargetId(targetId);
		request.setOperation(Operation.DISCOVERY);
		return CSE.doRequest(request);
	}
	
	public static ResponsePrimitive createAE(AE resource, String name){
		return createResource("/" + Constants.CSE_ID, name, resource, ResourceType.AE);
	}
	
	public static ResponsePrimitive createContainer(String targetId, String name, Container resource){
		return createResource(targetId, name, resource, ResourceType.CONTAINER);
	}
	
	public static ResponsePrimitive createContentInstance(String targetId, String name, ContentInstance resource){
		return createResource(targetId, name, resource, ResourceType.CONTENT_INSTANCE);
	}
	
	public static ResponsePrimitive createContentInstance(String targetId, ContentInstance resource){
		return createContentInstance(targetId, null, resource);
	}
	
	public static ResponsePrimitive deleteCI(String targetId) {
		RequestPrimitive request = new RequestPrimitive();
		request.setFrom(Constants.ADMIN_REQUESTING_ENTITY);
		//request.setTo(targetId);
		request.setTargetId(targetId);
		
		request.setOperation(Operation.DELETE);
		//request.setResourceType(BigInteger.valueOf(resourceType));
		//request.setRequestContentType(MimeMediaType.OBJ);
		//request.setReturnContentType(MimeMediaType.OBJ);
		//request.setContent(resource);
		//request.setName(name);
		return CSE.doRequest(request);
	}

	public static ResponsePrimitive getRequest(String targetId){
		RequestPrimitive request = new RequestPrimitive();
		request.setFrom(Constants.ADMIN_REQUESTING_ENTITY);
		request.setTargetId(targetId);
		request.setReturnContentType(MimeMediaType.OBJ);
		request.setOperation(Operation.RETRIEVE);
		request.setRequestContentType(MimeMediaType.OBJ);
		return CSE.doRequest(request);
	}
	
	public static ResponsePrimitive updateContainer(String target, String newName) {
		RequestPrimitive request = new RequestPrimitive();
		request.setFrom(Constants.ADMIN_REQUESTING_ENTITY);
		request.setTargetId(target);
		request.setOperation(Operation.UPDATE);
		request.setResourceType(BigInteger.valueOf(ResourceType.CONTAINER));
		request.setName(newName);
		//request.setRequestContentType(MimeMediaType.OBJ);
		request.setReturnContentType(MimeMediaType.OBJ);
		return CSE.doRequest(request);
	}
}
