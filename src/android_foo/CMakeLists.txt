##############################################################################
# CMake
##############################################################################

cmake_minimum_required(VERSION 2.8.3)
project(android_foo)

##############################################################################
# Catkin
##############################################################################

find_package(catkin REQUIRED rosjava_build_tools)
# Set the gradle targets you want catkin's make to run by default
# e.g. usually catkin_android_setup(assembleRelease uploadArchives)
catkin_android_setup(assembleRelease uploadArchives)
catkin_package()


##############################################################################
# Installation
##############################################################################

# Deploy android libraries (.aar's) and applications (.apk's)
install(DIRECTORY ${CATKIN_DEVEL_PREFIX}/${CATKIN_GLOBAL_MAVEN_DESTINATION}/com/github/rosjava/${PROJECT_NAME}/ 
       DESTINATION ${CATKIN_GLOBAL_MAVEN_DESTINATION}/com/github/rosjava/${PROJECT_NAME}/)
