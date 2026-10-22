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
#
# PDFBox also scans every installed font the first time a standard 14 font is used
# and writes the result to `.pdfbox.cache`. The `fonts` and `fontcache` stages below
# build that cache at image build time, against the exact fonts the runtime image
# uses, so the application starts (and serves its first PDF) without scanning fonts.
###

# Shared font tree: the cache builder and the runtime image copy the same files, so
# the absolute paths and contents in the cache stay valid.
FROM quay.io/rockylinux/rockylinux:9-minimal AS fonts
USER root
RUN microdnf install -y liberation-sans-fonts liberation-serif-fonts liberation-mono-fonts \
    && microdnf clean all

# Builds the PDFBox font cache once, with the same fonts as the runtime image.
FROM quay.io/rockylinux/rockylinux:9-minimal AS fontcache
USER root
RUN microdnf install -y java-17-openjdk-headless \
    && microdnf clean all \
    && rm -rf /usr/share/fonts
COPY --from=fonts /usr/share/fonts /usr/share/fonts
COPY build/fontcache-tool /tool
RUN mkdir -p /fontcache \
    && java -Dpdfbox.fontcache=/fontcache -cp "/tool:/tool/*" id.my.agungdh.fontcache.FontCacheBuilder \
    && test -s /fontcache/.pdfbox.cache

FROM quay.io/rockylinux/rockylinux:9-minimal
USER root
COPY --from=fonts /usr/share/fonts /usr/share/fonts
WORKDIR /work/
RUN mkdir -p /work/fontcache \
    && chown -R 1001:root /work \
    && chmod -R "g+rwX" /work
COPY --chown=1001:root --chmod=0644 --from=fontcache /fontcache/.pdfbox.cache /work/fontcache/.pdfbox.cache
COPY --chown=1001:root --chmod=0755 build/*-runner /work/application
COPY --chown=1001:root --chmod=0755 build/*.so /work/

EXPOSE 8080
USER 1001

ENTRYPOINT ["./application", "-Dquarkus.http.host=0.0.0.0", "-Dpdfbox.fontcache=/work/fontcache"]
