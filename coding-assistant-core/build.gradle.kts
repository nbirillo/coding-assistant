tasks {
    jar {
        from(sourceSets["main"].allSource)
        archiveClassifier.set("sources")
    }
}
