####
# This Dockerfile is used in order to build a container that runs the Quarkus application in native (no JVM) mode.
# It uses the UBI 9 minimal base image, which provides the native runtime libraries.
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
# PDFBox maps the standard 14 fonts to metric-compatible system fonts. The UBI
# repositories only provide Liberation Mono, so the complete Liberation family is
# taken from a CentOS Stream 9 build stage. Without those fonts PDFBox falls back
# to its built-in Liberation Sans and logs a warning for every base font.
###
FROM quay.io/centos/centos:stream9 AS fonts
RUN dnf -q install -y liberation-sans-fonts liberation-serif-fonts liberation-mono-fonts \
    && dnf -q clean all

FROM registry.access.redhat.com/ubi9/ubi-minimal:9.8
USER root
COPY --from=fonts /usr/share/fonts/liberation-sans /usr/share/fonts/liberation-sans
COPY --from=fonts /usr/share/fonts/liberation-serif /usr/share/fonts/liberation-serif
COPY --from=fonts /usr/share/fonts/liberation-mono /usr/share/fonts/liberation-mono
WORKDIR /work/
RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work
COPY --chown=1001:root --chmod=0755 build/*-runner /work/application
COPY --chown=1001:root --chmod=0755 build/*.so /work/

EXPOSE 8080
USER 1001

ENTRYPOINT ["./application", "-Dquarkus.http.host=0.0.0.0"]
