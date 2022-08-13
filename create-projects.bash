mkdir microservices
cd microservices

spring init \
--build=gradle \
--name=review-service \
--package-name=me.blessedsibanda.microservices.core.review \
--groupId=me.blessedsibanda.microservices.core.review \
--dependencies=actuator,webflux \
review-service

spring init \
--build=gradle \
--name=product-service \
--package-name=me.blessedsibanda.microservices.core.product \
--groupId=me.blessedsibanda.microservices.core.product \
--dependencies=actuator,webflux \
product-service

spring init \
--build=gradle \
--name=recommendation-service \
--package-name=me.blessedsibanda.microservices.core.recommendation \
--groupId=me.blessedsibanda.microservices.core.recommendation \
--dependencies=actuator,webflux \
recommendation-service

spring init \
--build=gradle \
--package-name=me.blessedsibanda.microservices.composite.product \
--groupId=me.blessedsibanda.microservices.composite.product \
--dependencies=actuator,webflux \
product-composite-service

cd ..

find microservices -depth -name "gradle" -exec rm -rfv "{}" \;
find microservices -depth -name "gradlew" -exec rm -fv "{}" \;
find microservices -depth -name "gradlew.bat" -exec rm -fv "{}" \;
find microservices -depth -name ".gitignore" -exec rm -fv "{}" \;
find microservices -depth -name "HELP.md" -exec rm -fv "{}" \;