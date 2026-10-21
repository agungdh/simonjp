####
# This Dockerfile is used in order to build a container that runs the Quarkus application in native (no JVM) mode.
# It uses a micro base image, tuned for Quarkus native executables.
# It reduces the size of the resulting container image.
# Check https://quarkus.io/guides/quarkus-runtime-base-image for further information about this image.
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
# The `quay.io/quarkus/ubi9-quarkus-micro-image:2.0` base image is based on UBI 9
# and intentionally ships without a package manager (no microdnf/dnf).
# To use UBI 8, switch to `quay.io/quarkus/quarkus-micro-image:2.0`.
###
FROM registry.access.redhat.com/ubi9/ubi-minimal:9.8 AS fonts
# Apache POI and Java2D chart rendering need font metrics and system fonts at runtime.
RUN microdnf install -y freetype fontconfig dejavu-sans-fonts \
    && microdnf clean all

FROM quay.io/quarkus/ubi9-quarkus-micro-image:2.0
USER root
# Copy the font stack from the stage above instead of using a package manager,
# which the micro image does not have. The library list is fontconfig/freetype
# plus their transitive dependencies (checked with `ldd`); the micro image
# already provides glibc, libz1 and the dynamic loader.
COPY --from=fonts /usr/lib64/libfontconfig.so.1* /usr/lib64/
COPY --from=fonts /usr/lib64/libfreetype.so.6* /usr/lib64/
COPY --from=fonts /usr/lib64/libxml2.so.2* /usr/lib64/
COPY --from=fonts /usr/lib64/libbz2.so.1* /usr/lib64/
COPY --from=fonts /usr/lib64/libpng16.so.16* /usr/lib64/
COPY --from=fonts /usr/lib64/libharfbuzz.so.0* /usr/lib64/
COPY --from=fonts /usr/lib64/libbrotlidec.so.1* /usr/lib64/
COPY --from=fonts /usr/lib64/libbrotlicommon.so.1* /usr/lib64/
COPY --from=fonts /usr/lib64/liblzma.so.5* /usr/lib64/
COPY --from=fonts /usr/lib64/libglib-2.0.so.0* /usr/lib64/
COPY --from=fonts /usr/lib64/libgraphite2.so.3* /usr/lib64/
COPY --from=fonts /usr/lib64/libpcre.so.1* /usr/lib64/
COPY --from=fonts /etc/fonts /etc/fonts
COPY --from=fonts /usr/share/fontconfig /usr/share/fontconfig
COPY --from=fonts /usr/share/fonts /usr/share/fonts
WORKDIR /work/
# fontconfig needs a writable cache directory, and the runtime user has no HOME.
ENV XDG_CACHE_HOME=/tmp
RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work
COPY --chown=1001:root --chmod=0755 build/*-runner /work/application
COPY --chown=1001:root --chmod=0755 build/*.so /work/

EXPOSE 8080
USER 1001

ENTRYPOINT ["./application", "-Dquarkus.http.host=0.0.0.0"]
