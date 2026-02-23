def call(String url,String branch){
  echo "Clonning the branch"
  git url: "${url}", branch: "${branch}"
  echo "Code clonning successful"
}
