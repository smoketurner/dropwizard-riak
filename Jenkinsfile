node {
  stage 'checkout'

  git url: 'https://github.com/smoketurner/dropwizard-riak.git'

  def mvnHome = tool 'Maven 3.3.9'

  stage 'build'

  sh "${mvnHome}/bin/mvn clean cobertura:cobertura findbugs:findbugs"

  step([$class: 'GitHubSetCommitStatusBuilder'])
  step([$class: 'PmdPublisher'])
  step([$class: 'FindBugsPublisher'])
  step([$class: 'CoberturaBugsPublisher'])
}