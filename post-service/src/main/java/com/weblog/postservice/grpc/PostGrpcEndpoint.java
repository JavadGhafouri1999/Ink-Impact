package com.weblog.postservice.grpc;

import com.weblog.postservice.service.PostService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
public class PostGrpcEndpoint extends PostServiceGrpc.PostServiceImplBase {
	
	private final PostService postService;
	
	public PostGrpcEndpoint(PostService postService) {
		this.postService = postService;
	}
	
	@Override
	public void postExists(PostExistsRequest request,
	                       StreamObserver <PostExistsResponse> responseObserver) {
		
		UUID    postId = UUID.fromString(request.getPostId());
		boolean exists = postService.existsById(postId);
		
		PostExistsResponse response = PostExistsResponse.newBuilder()
		                                                .setExists(exists)
		                                                .build();
		
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
}
