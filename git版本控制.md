# git版本控制

1. 在项目跟目录执行git init初始化
2. 新建.gitignore文件里面写入排除不想上传的文件
3. git status 查看修改的内容
4. git add .暂存修复的内容
5. git commit -m "提交说明"
6. 3到5是分支提交，一般用来提交更改，要是代码运行无误，有创新可以更新到主分支中，如果有误继续修改，然后执行3到5，不过第五步加个参数git commit --amend -m "..."
7. 提交到主分支：git checkout main
8. 合并开发分支：git merge dev
9. 打上成功标签（可选但推荐）git tag ....，以后可以通过git check ...回到当前版本。
10. 切回dev分支：git checkout dev。



## git常用命令：

git checkout -b 分支名：创建分支

git branch -a 查看所有分支

git branch -m 名字：更改当前分支名

git push -u origin dev：`-u` 选项会将本地的 `dev` 分支与远程的 `dev` 分支关联起来，方便后续的 `git push` 和 `git pull` 操作。

git push origin --delete master：**删除远程的 `master` 分支**

git remote -v查看当前关联的远程仓库信息

git push -u origin main/dev：推送到远程仓库

git tag ：查看所有版本号

git merge --abort：回到安全

git push origin 标签名：给远程仓库发送标签