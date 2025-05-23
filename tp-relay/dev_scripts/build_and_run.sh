cd ..
docker build -t tp-relay . &&
docker run -p 9090:9090 tp-relay