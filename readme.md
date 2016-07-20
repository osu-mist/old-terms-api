# Terms API

Returns terms used to search for classes in the class-search api

### Generate Keys

HTTPS is required for Web APIs in development and production. Use `keytool(1)` to generate public and private keys.

Generate key pair and keystore:

    $ keytool \
        -genkeypair \
        -dname "CN=Jane Doe, OU=Enterprise Computing Services, O=Oregon State University, L=Corvallis, S=Oregon, C=US" \
        -ext "san=dns:localhost,ip:127.0.0.1" \
        -alias doej \
        -keyalg RSA \
        -keysize 2048 \
        -sigalg SHA256withRSA \
        -validity 365 \
        -keystore doej.keystore

Export certificate to file:

    $ keytool \
        -exportcert \
        -rfc \
        -alias "doej" \
        -keystore doej.keystore \
        -file doej.pem

Import certificate into truststore:

    $ keytool \
        -importcert \
        -alias "doej" \
        -file doej.pem \
        -keystore doej.truststore

## Gradle

This project uses the build automation tool Gradle. Use the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) to download and install it automatically:

    $ ./gradlew

The Gradle wrapper installs Gradle in the directory `~/.gradle`. To add it to your `$PATH`, add the following line to `~/.bashrc`:

    $ export PATH=$PATH:/home/user/.gradle/wrapper/dists/gradle-2.4-all/WRAPPER_GENERATED_HASH/gradle-2.4/bin

The changes will take effect once you restart the terminal or `source ~/.bashrc`.

## Tasks

List all tasks runnable from root project:

    $ gradle tasks

### IntelliJ IDEA

Generate IntelliJ IDEA project:

    $ gradle idea

Open with `File` -> `Open Project`.

### Configure

Copy [configuration-example.yaml](configuration-example.yaml) to `configuration.yaml`. Modify as necessary, being careful to avoid committing sensitive data.

### Build

Build the project:

    $ gradle build

JARs [will be saved](https://github.com/johnrengelman/shadow#using-the-default-plugin-task) into the directory `build/libs/`.

### Run

Run the project:

    $ gradle run


## Base an Existing Project off the Skeleton

Add the skeleton as a remote:

    $ git remote add skeleton git@github.com:osu-mist/web-api-skeleton.git
    $ git fetch skeleton

Create a branch to track the skeleton:

    $ git checkout -b skeleton-master skeleton/master

Merge the skeleton into your codebase:

    $ git checkout feature/abc-123-branch
    $ git merge skeleton-master
    ...
    $ git commit -v


## Incorporate Updates from the Skeleton

Ensure that branch `skeleton-master` is tracking remote `skeleton`:

    $ git branch -u skeleton/master skeleton-master

Update local branch:

    $ git fetch skeleton
    $ git pull

Merge the updates into your codebase as before. Note that changes to CodeNarc configuration may introduce build failures.


## Resources

The Web API definition is contained in the [Swagger specification](swagger.yaml).

The following examples demonstrate the use of `curl` to make authenticated HTTPS requests.

### GET /

This resource returns build and runtime information:

    $ curl \
    > --cacert doej.pem \
    > --user "username:password" \
    > https://localhost:8080/api/v0/
    {"name":"terms-api","time":1468976102073,"commit":"59f9ed6","documentation":"swagger.yaml"}

### GET /terms

This resource returns a list of terms based on the parameters:

    $ curl \
    > --cacert doej.pem \
    > --user "username:password" \
    > https://localhost:8080/api/v0/terms?page[size]=1
    {"links":{"self":"https://api.oregonstate.edu/v1/terms/terms?page[number]=1&page[size]=1","first":"https://api.oregonstate.edu/v1/terms/terms?page[number]=1&page[size]=1","last":"https://api.oregonstate.edu/v1/terms/terms?page[number]=394&page[size]=1","prev":null,"next":"https://api.oregonstate.edu/v1/terms/terms?page[number]=2&page[size]=1"},"data":[{"id":"198601","type":"term","attributes":{"code":"198601","description":"Fall 1985","startDate":"1985-09-26","endDate":"1985-12-20","financialAidYear":"8586","housingStartDate":"1985-08-28","housingEndDate":"1985-12-12"},"links":null}]}

### GET /terms/{code}

This resource returns a single resource specified by the code:

    $ curl \
    > --cacert doej.pem \
    > --user "username:password" \
    > https://localhost:8080/api/v0/terms/999999
{"links":{},"data":{"id":"999999","type":"term","attributes":{"code":"999999","description":"The End of Time","startDate":"2999-06-08","endDate":"2999-12-31","financialAidYear":"9999","housingStartDate":"2999-06-09","housingEndDate":"2999-12-31"},"links":null}}

### GET /terms/open

This resource returns the terms open for registration:

    $ curl \
    > --cacert doej.pem \
    > --user "username:password" \
    > https://localhost:8080/api/v0/terms/open
    {"links":{},"data":[{"id":"201701","type":"term","attributes":{"code":"201701","description":"Fall 2016","startDate":null,"endDate":null,"financialAidYear":null,"housingStartDate":null,"housingEndDate":null},"links":null},{"id":"201700","type":"term","attributes":{"code":"201700","description":"Summer 2016","startDate":null,"endDate":null,"financialAidYear":null,"housingStartDate":null,"housingEndDate":null},"links":null}]}


NOTE: you should only specify a certificate with --cacert for local testing.
Production servers should use a real certificate
issued by a valid certificate authority.

