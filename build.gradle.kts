plugins {
    groovy
    java
    maven
}

group = "com.github.caoli5288"

version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.spigotmc:spigot:1.12.2-R0.1-SNAPSHOT")
    implementation("org.codehaus.groovy:groovy:3.0.4")
    implementation("org.projectlombok:lombok:1.18.12")
    annotationProcessor("org.projectlombok:lombok:1.18.12")
}