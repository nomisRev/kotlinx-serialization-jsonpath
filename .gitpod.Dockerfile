FROM gitpod/workspace-full-vnc

USER gitpod

RUN sudo apt-get update && \
    sudo apt-get install -yq chromium-browser && \
    sudo apt-get install -yq chromium && \
    sudo apt-get install -yq firefox && \
    sudo rm -rf /var/lib/apt/lists/*

ENV CHROME_BIN=/usr/bin/chromium-browser
ENV BROWSER="ChromeHeadless"
