plugins {
    id("com.falsepattern.fpgradle-mc") version ("0.19.3")
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
    exclusive(mega(), "mega", "codechicken")
}

dependencies {
    implementation("it.unimi.dsi:fastutil:8.5.16")
    runtimeOnlyNonPublishable("codechicken:notenoughitems-mc1.7.10:2.7.51-mega:dev")
    runtimeOnlyNonPublishable("codechicken:codechickencore-mc1.7.10:1.4.2-mega:dev")

    devOnlyNonPublishable("mega:carpentersblocks-mc1.7.10:3.7.0-mega:dev")
}