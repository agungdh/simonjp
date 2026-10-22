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
#
# PDFBox maps the standard 14 fonts to metric-compatible system fonts. Rocky Linux
# provides the complete Liberation family, so installing it lets those mappings
# resolve without PDFBox falling back to its built-in Liberation Sans and logging
# a warning for every base font.
###
FROM quay.io/rockylinux/rockylinux:9-minimal
USER root
RUN microdnf install -y liberation-sans-fonts liberation-serif-fonts liberation-mono-fonts \
    && microdnf clean all
WORKDIR /work/
RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work
COPY --chown=1001:root --chmod=0755 build/*-runner /work/application
COPY --chown=1001:root --chmod=0755 build/*.so /work/

EXPOSE 8080
USER 1001

ENTRYPOINT ["./application", "-Dquarkus.http.host=0.0.0.0"]
