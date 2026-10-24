####
# This Dockerfile runs the Spring Boot application in native (no JVM) mode.
# The runtime is the distroless Debian 13 (trixie) image, which ships only glibc, zlib,
# libstdc++/libgcc, OpenSSL and CA certificates - no shell and no package manager.
# Check https://github.com/GoogleContainerTools/distroless for the available tags.
#
# Before building the container image run:
#
# ./gradlew nativeCompile
#
# Then, build the image with:
#
# docker build -t simonjp .
#
# Then run the container using:
#
# docker run -i --rm -p 8080:8080 simonjp
###

FROM gcr.io/distroless/cc-debian13:nonroot

WORKDIR /work/
COPY --chown=nonroot:nonroot --chmod=0755 build/native/nativeCompile/simonjp /work/application

# Spring Boot reads server.port from the SERVER_PORT environment variable.
ENV SERVER_PORT=8080

EXPOSE 8080
USER nonroot

ENTRYPOINT ["/work/application"]