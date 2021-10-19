VERSION = $(shell ruby -r rexml/document -e 'puts REXML::Document.new(File.new(ARGV.shift)).elements["/project/version"].text' pom.xml)

##@ MAIN

.PHONY: build

build: ## Building project
	./mvnw clean install

build-no-test: ## Building project
	./mvnw clean install -DskipTests

version: ## Release new version
	@echo $(VERSION)

release: ## Release new version
	./mvnw -B release:clean release:prepare release:perform

release-rollback: ## Rollback with new rollback command
	./mvnw -B release:rollback

release-rollback-manually: ## Rollback manually
	git tag -d azure-cosmosdb-junit-extension-1.0.0
	git push --delete origin azure-cosmosdb-junit-extension-1.0.0
	git reset --hard HEAD~2
	git push origin main -f

