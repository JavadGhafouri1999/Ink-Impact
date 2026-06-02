package com.weblog.commentservice.grpc;


import com.weblog.commentservice.post.grpc.PostServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

  @Bean
  public ManagedChannel postServiceChannel() {
	return ManagedChannelBuilder
			.forAddress("post-service", 9090) // hostname of PostService in Docker
			.usePlaintext()
			.build();
  }

  @Bean
  public PostServiceGrpc.PostServiceBlockingStub postServiceStub(
		  ManagedChannel postServiceChannel) {
	return PostServiceGrpc.newBlockingStub(postServiceChannel);
  }
}
