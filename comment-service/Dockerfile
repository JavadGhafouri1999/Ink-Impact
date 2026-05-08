# -------------------------
# Build stage
# -------------------------
# We select a runer something that can build and create the app here it's maven
FROM maven:3.9.9-eclipse-temurin-21 AS builder

# Inside our container there must be a place which our app works and takes its data /app is the simplest one
WORKDIR /app

# We copy our dependencies inside our working directory . or ./ so our container can access it
COPY pom.xml .

# We now need to run the builder so it takes the deps and create the app inside ./app
RUN mvn dependency:go-offline -q

COPY src ./src

RUN mvn clean package -DskipTests

# -------------------------
# Run stage
# -------------------------
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

COPY --from=builder /app/target/comment-service-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 4001
ENTRYPOINT ["java", "-jar", "app.jar"]
