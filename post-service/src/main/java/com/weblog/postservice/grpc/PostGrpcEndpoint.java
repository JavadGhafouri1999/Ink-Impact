package com.weblog.postservice.grpc;

import com.weblog.postservice.service.PostServiceImpl;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
public class PostGrpcEndpoint extends PostServiceGrpc.PostServiceImplBase {
	
	private final PostServiceImpl postService;
	
	@Override
	public void postExists(PostExistsRequest request,
	                       StreamObserver <PostExistsResponse> responseObserver) {
		
		UUID    postId = UUID.fromString(request.getPostId());
		boolean exists = postService.existsById(postId);
		
		PostExistsResponse response = PostExistsResponse
				.newBuilder()
				.setExists(exists)
				.build();
		
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
}
