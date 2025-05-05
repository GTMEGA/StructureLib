plugins {
    id("fpgradle-minecraft") version ("0.10.0")
}

group = "mega"

minecraft_fp {
    mod {
        modid = "structurelib"
        name = "StructureLib"
        rootPkg = "com.gtnewhorizon.structurelib"
    }

    tokens {
        tokenClass = "Tags"
        modid = "MODID"
        name = "MODNAME"
        version = "VERSION"
        rootPkg = "GROUPNAME"
    }

    publish {
        maven {
            repoUrl = "https://mvn.falsepattern.com/gtmega_releases/"
            repoName = "mega"
        }
    }
}

repositories {
    mega()
}

dependencies {
    implementation("it.unimi.dsi:fastutil:8.5.13")
    runtimeOnlyNonPublishable("codechicken:notenoughitems-mc1.7.10:2.3.1-mega:dev")
    runtimeOnlyNonPublishable("codechicken:codechickencore-mc1.7.10:1.4.0-mega:dev")

    devOnlyNonPublishable("mega:carpentersblocks-mc1.7.10:3.4.1-mega:dev")
}