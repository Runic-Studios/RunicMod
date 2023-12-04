dependencies {
    compileOnly(commonLibs.spigot)
    compileOnly(commonLibs.acf)
    compileOnly(commonLibs.paper)
    compileOnly(commonLibs.taskchain)
    compileOnly(project(":Projects:Common"))
    compileOnly(project(":Projects:Items"))
    compileOnly(project(":Projects:Core"))
    compileOnly(project(":Projects:Chat"))
    compileOnly(project(":Projects:Database"))
    compileOnly(project(":Projects:Bank"))
    compileOnly(project(":Projects:PvP"))
    compileOnly(project(":Projects:Npcs"))
}