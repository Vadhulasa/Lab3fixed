# DO NOT touch
# run your container
docker container rm  lbs-android || echo "Service does not exist, creating"
docker run -it -p 12345:12345 --name lbs-android aahmadian/lbs-android:1.1