####
# This Dockerfile builds a container that runs the Quarkus application in native (no JVM) mode.
# The runtime is the distroless Debian 13 (trixie) image, which ships only glibc, zlib,
# libstdc++/libgcc, OpenSSL and CA certificates - no shell and no package manager.
# Check https://github.com/GoogleContainerTools/distroless for the available tags.
#
# Before building the container image run:
#
# ./gradlew build -Dquarkus.native.enabled=true -Dquarkus.package.jar.enabled=false
#
# Then, build the image with:
#
# docker build -t quarkus/simonjp .
#
# Then run the container using:
#
# docker run -i --rm -p 8080:8080 quarkus/simonjp
###

FROM gcr.io/distroless/cc-debian13:nonroot

WORKDIR /work/
COPY --chown=nonroot:nonroot --chmod=0755 build/*-runner /work/application

# Native executables read runtime configuration from environment variables too,
# so the container does not depend on shell-style -D arguments.
ENV QUARKUS_HTTP_HOST=0.0.0.0

EXPOSE 8080
USER nonroot

ENTRYPOINT ["/work/application"]
