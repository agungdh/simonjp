####
# This Dockerfile is used in order to build a container that runs the Quarkus application in native (no JVM) mode.
# It uses the Rocky Linux 9 minimal base image, which provides the native runtime libraries.
# Check https://quarkus.io/guides/quarkus-runtime-base-image for further information about native images.
#
# Before building the container image run:
#
# ./gradlew build -Dquarkus.native.enabled=true
#
# Then, build the image with:
#
# docker build -t quarkus/simonjp .
#
# Then run the container using:
#
# docker run -i --rm -p 8080:8080 quarkus/simonjp
###

FROM quay.io/rockylinux/rockylinux:9-minimal
USER root
WORKDIR /work/
RUN chown -R 1001:root /work \
    && chmod -R "g+rwX" /work
COPY --chown=1001:root --chmod=0755 build/*-runner /work/application

EXPOSE 8080
USER 1001

ENTRYPOINT ["./application", "-Dquarkus.http.host=0.0.0.0"]
