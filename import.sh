# this script imports the various dependencies that are difficult to build and are not being actively developed at this point

mvn install:install-file -Dfile=kinoath-stable.app/Contents/MacOS/lib/imdi-api-1.1.2.jar -DgroupId=nl.mpi -DartifactId=imdi-api -Dversion=1.1.2 -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-stable.app/Contents/MacOS/lib/kinnate-plugins-export-1.0.37446-stable.jar -DgroupId=nl.mpi -DartifactId=kinnate-plugins-export -Dversion=1.0.37446-stable -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-stable.app/Contents/MacOS/lib/typechecker-1.7.0.jar -DgroupId=nl.mpi -DartifactId=typechecker -Dversion=1.7.0 -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-stable.app/Contents/MacOS/lib/kinoath-help-1.4.38281-stable.jar -DgroupId=nl.mpi -DartifactId=kinoath-help -Dversion=1.4.38281-stable -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-stable.app/Contents/MacOS/lib/kinoath-localisation-1.4.39338-stable.jar -DgroupId=nl.mpi -DartifactId=kinoath-localisation -Dversion=1.4.39338-stable -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-stable.app/Contents/MacOS/lib/mpi-util-1.0.0.jar -DgroupId=nl.mpi -DartifactId=mpi-util -Dversion=1.0.0 -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-stable.app/Contents/MacOS/lib/corpusstructure-api-1.7.3.jar -DgroupId=nl.mpi -DartifactId=corpusstructure-api -Dversion=1.7.3 -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-stable.app/Contents/MacOS/lib/pluginloader-1.1.35804-stable.jar -DgroupId=nl.mpi -DartifactId=pluginloader -Dversion=1.1.35804-stable -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-stable.app/Contents/MacOS/lib/plugins-core-1.1.35861-stable.jar -DgroupId=nl.mpi -DartifactId=plugins-core -Dversion=1.1.35861-stable -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-stable.app/Contents/MacOS/lib/arbil-2.5.37662-stable.jar -DgroupId=nl.mpi -DartifactId=arbil -Dversion=2.5.37662-stable -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-stable.app/Contents/MacOS/lib/arbil-commons-2.5.35829-stable.jar -DgroupId=nl.mpi -DartifactId=arbil-commons -Dversion=2.5.35829-stable -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-stable.app/Contents/MacOS/lib/arbil-help-2.5.37839-stable.jar -DgroupId=nl.mpi -DartifactId=arbil-help -Dversion=2.5.37839-stable -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-stable.app/Contents/MacOS/lib/arbil-localisation-1.0-SNAPSHOT.jar -DgroupId=nl.mpi -DartifactId=arbil-localisation -Dversion=1.0-SNAPSHOT -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-stable.app/Contents/MacOS/kinoath-stable-1-4-39349.jar -DgroupId=nl.mpi -DartifactId=kinoath -Dversion=1-4-39349-stable -Dpackaging=jar -Dmaven.repo.local=$pwd;

mvn install:install-file -Dfile=kinoath-testing.app/Contents/MacOS/kinoath-testing-1-5-2261.jar -DgroupId=nl.mpi -DartifactId=kinoath -Dversion=1-5-2261-testing -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-testing.app/Contents/MacOS/lib/imdi-api-1.1.4.jar -DgroupId=nl.mpi -DartifactId=imdi-api -Dversion=1.1.4 -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-testing.app/Contents/MacOS/lib/arbil-2.5.1039-stable.jar -DgroupId=nl.mpi -DartifactId=arbil -Dversion=2.5.1039-stable -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-testing.app/Contents/MacOS/lib/kinnate-plugins-export-1.1.42-testing.jar -DgroupId=nl.mpi -DartifactId=kinnate-plugins-export -Dversion=1.1.42-testing -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-testing.app/Contents/MacOS/lib/arbil-commons-2.5.39-stable.jar -DgroupId=nl.mpi -DartifactId=arbil-commons -Dversion=2.5.39-stable -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-testing.app/Contents/MacOS/lib/kinoath-help-1.5.28-testing.jar -DgroupId=nl.mpi -DartifactId=kinoath-help -Dversion=1.5.28-testing -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-testing.app/Contents/MacOS/lib/typechecker-1.7.2.jar -DgroupId=nl.mpi -DartifactId=typechecker -Dversion=1.7.2 -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-testing.app/Contents/MacOS/lib/arbil-help-2.5.32-stable.jar -DgroupId=nl.mpi -DartifactId=arbil-help -Dversion=2.5.32-stable -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-testing.app/Contents/MacOS/lib/kinoath-localisation-1.5.67-testing.jar -DgroupId=nl.mpi -DartifactId=kinoath-localisation -Dversion=1.5.67-testing -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-testing.app/Contents/MacOS/lib/arbil-localisation-2.5.145-stable.jar -DgroupId=nl.mpi -DartifactId=arbil-localisation -Dversion=2.5.145-stable -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-testing.app/Contents/MacOS/lib/mpi-util-1.2.3.jar -DgroupId=nl.mpi -DartifactId=mpi-util -Dversion=1.2.3 -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-testing.app/Contents/MacOS/lib/pluginloader-1.1.35804-stable.jar -DgroupId=nl.mpi -DartifactId=pluginloader -Dversion=1.1.35804-stable -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-testing.app/Contents/MacOS/lib/plugins-core-1.2.113-stable.jar -DgroupId=nl.mpi -DartifactId=plugins-core -Dversion=1.2.113-stable -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-testing.app/Contents/MacOS/lib/corpusstructure-api-1.8.1.jar -DgroupId=nl.mpi -DartifactId=corpusstructure-api -Dversion=api-1.8.1 -Dpackaging=jar -Dmaven.repo.local=$pwd;
mvn install:install-file -Dfile=kinoath-testing.app/Contents/MacOS/lib/slcshttps-0.2.jar -DgroupId=nl.nikhef -DartifactId=slcshttps -Dversion=0.2 -Dpackaging=jar -Dmaven.repo.local=$pwd;
