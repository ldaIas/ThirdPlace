package com.thirdplace.endpoints;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import com.thirdplace.services.RSVPsService;
import com.thirdplace.services.RSVPsService.CreateRSVPRequest;
import com.thirdplace.services.RSVPsService.UpdateRSVPStatusRequest;
import com.thirdplace.services.RSVPsService.RespondToRSVPRequest;

@Path("")
public class RSVPsEndpoints {

    @POST
    @Path("api/RSVPs:create")
    public Response createRSVP(final CreateRSVPRequest request) {
        return EndpointsBase.processRequest(() -> RSVPsService.createRSVP(request));
    }

    @GET
    @Path("api/RSVPs:getByPost/{postId}")
    public Response getRSVPsByPost(@PathParam("postId") final String postId) {
        return EndpointsBase.processRequest(() -> RSVPsService.getRSVPsByPost(postId));
    }

    @GET
    @Path("api/RSVPs:getByUser/{userId}")
    public Response getRSVPsByUser(@PathParam("userId") final String userId) {
        return EndpointsBase.processRequest(() -> RSVPsService.getRSVPsByUser(userId));
    }

    @PUT
    @Path("api/RSVPs:updateStatus")
    public Response updateRSVPStatus(final UpdateRSVPStatusRequest request) {
        return EndpointsBase.processRequest(() -> RSVPsService.updateRSVPStatus(request));
    }

    @DELETE
    @Path("api/RSVPs:delete/{rsvpId}")
    public Response deleteRSVP(@PathParam("rsvpId") final String rsvpId) {
        return EndpointsBase.processRequest(() -> RSVPsService.deleteRSVP(rsvpId));
    }

    @PUT
    @Path("api/RSVPs:respond")
    public Response respondToRSVP(final RespondToRSVPRequest request) {
        return EndpointsBase.processRequest(() -> RSVPsService.respondToRSVP(request));
    }
}