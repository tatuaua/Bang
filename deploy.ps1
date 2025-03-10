# deploy.ps1
Set-Location -Path "C:\Coding\bang"
git pull origin main
docker-compose down
docker-compose up -d --build
Write-Output "$(Get-Date) - Deployment completed" >> "C:\Coding\bang\deploy.log"