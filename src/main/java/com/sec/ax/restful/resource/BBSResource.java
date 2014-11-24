package com.sec.ax.restful.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sec.ax.restful.annotation.RolesAllowed;
import com.sec.ax.restful.common.Constant;
import com.sec.ax.restful.pojo.ResponseElement;
import com.sec.ax.restful.pojo.Role;
import com.sec.ax.restful.service.BBSService;
import com.sec.ax.restful.utils.FileHelper;
import com.sec.ax.restful.utils.FormatHelper;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

/**
 * 
 * @author heesik.jeon
 *
 */

@Component
@Path("/bbs")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
public class BBSResource extends AbstractResource {
	
    private static final Logger logger = Logger.getLogger(BBSResource.class);
    
    @Autowired
    private BBSService service;

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({Role.Admin,Role.User})
    public ResponseElement uploadFile(@FormDataParam("file") InputStream is, @FormDataParam("file") FormDataContentDisposition fdcd) {
    	
        logger.debug("..");
        
        Object object = new Object();
        
        try {
        	
        	object = service.uploadFile(getUserPrincipal(), is, fdcd.getFileName());
        	
        	if (object == null) {
            	exceptionManager.fireUserException(Constant.ERR_FILE_MISSING, null);
        	}
        	
        } catch (java.io.FileNotFoundException e) {
        	exceptionManager.fireUserException(Constant.ERR_FILE_MISSING, null);
        } catch (IOException e) {
        	exceptionManager.fireSystemException(new Exception(e));
        }

        logger.debug(FormatHelper.printPretty(object));
        
        return ResponseElement.newSuccessInstance(object);

    }
    
    @GET
    @Path("/download/{filename}")
    @Produces()
    @RolesAllowed({Role.Admin,Role.User})
    public Response downloadFile(@PathParam("filename") String filename) {
    	
        logger.debug("..");
        
        try {
        	
            String hash = FileHelper.hashdir(Constant.FILE_BASE_PATH, getUserPrincipal().getName(), Constant.FILE_BASE_DEPTH);
            String filepath = new StringBuffer(hash).append(filename).toString();

        	File file = new File(filepath);
        	
        	if (file.exists()) {
        		
        		ResponseBuilder response = Response.ok(((Object) file));
        		response.header("Content-Disposition", "attachment; filename="+filename);
        		
        		return response.build();
        		
        	}
        	
        } catch (Exception e) {
        	exceptionManager.fireSystemException(new Exception(e));
        }

        ResponseBuilder response = Response.status(Status.BAD_REQUEST);
        
        return response.build(); 
        
    }
    
}
