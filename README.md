temporary readme file.

1. Create your `.env` file, specify the ALLOWED_ORIGIN value if needed. `.env.sample` present as an example which could
   be used on the local machine just by renaming to `.env`.
2. Correct the environment variable ALLOWED_ORIGIN value in the Dockerfile to the necessary origin URI
3. Open the command line from the project's root folder and build the Docker image using the following commands:

```bash
mvn clean install
````

```bash 
docker build -t report:pdfReport . 
```

after building the image, execute the following command to run the Docker container. You can replace '8081' with your
desired free port:

```bash 
docker run -d -p 8081:8080 report:pdfReport 

```