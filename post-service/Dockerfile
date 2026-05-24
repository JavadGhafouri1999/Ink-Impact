# -------------------------
# Build stage
# -------------------------
# We select a runer something that can build and create the app here it's maven
FROM maven:3.9.9-eclipse-temurin-21 AS builder

# Inside our container there must be a place which our app works and takes its data /app is the simplest one
WORKDIR /app

# Configure Maven with MVNHub mirror (Iranian mirror)
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>\n\
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"\n\
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"\n\
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">\n\
    <mirrors>\n\
        <mirror>\n\
            <id>mvnhub</id>\n\
            <url>https://mvnhub.ir/</url>\n\
            <mirrorOf>central,*,!my-internal</mirrorOf>\n\
        </mirror>\n\
    </mirrors>\n\
</settings>' > /root/.m2/settings.xml
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

COPY --from=builder /app/target/post-service-0.0.1-SNAPSHOT.jar ./app.jar

EXPOSE 4000

ENTRYPOINT ["java", "-jar", "app.jar"]
