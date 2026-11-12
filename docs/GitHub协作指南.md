# 小米便签 GitHub 协作操作指南

适用于费枭健、毕子健、梁冠杰的小组仓库。仓库地址为 <https://github.com/xiaojian-fei/micode-notes-course>。

## 1. 先理解这些词

| 名称 | 含义 | 本项目怎么用 |
| --- | --- | --- |
| Git | 在电脑上记录文件修改历史的工具 | 查看、提交和合并代码或文档改动 |
| GitHub | 在线托管 Git 仓库的平台 | 共享源码、查看分支和讨论修改 |
| Repository / Repo | 仓库，包含项目文件和历史 | 本项目的 micode-notes-course |
| Clone | 把远程仓库和历史下载到本机 | 第一次获取项目 |
| Branch | 分支，指向一条可继续提交的历史 | 每项任务创建一个分支 |
| main | 当前项目的默认分支 | 集成并确认后的公共版本 |
| Commit | 本地提交，为选中的修改建立一条历史记录 | 完成一个明确的小改动后提交 |
| Stage / git add | 选择哪些改动进入下一次提交 | 指定本次要交的文件 |
| Push | 将本地提交上传到远程分支 | 上传自己的任务分支 |
| Fetch | 获取远程最新历史和分支信息 | 查看别人是否有更新 |
| Pull | 获取远程历史并整合到当前分支 | 更新本地 main |
| Pull Request / PR | 在 GitHub 提议把某分支合入目标分支 | 队友提交后请费枭健检查 |
| Review | 检查 PR 中的改动并反馈 | 合并前核对内容及验证结果 |
| Merge | 合并分支历史 | 把确认后的改动集成到 main |
| Fork | 在自己的 GitHub 账号下创建仓库副本 | 没有原仓库写权限时提交贡献 |

本地保存文件后，依次经过 add、commit、push，修改才会出现在 GitHub。push 到任务分支后，还要通过 PR 合并，才会进入 main。

## 2. 你们小组采用什么流程

推荐流程为“更新 main → 创建任务分支 → 修改 → 本地检查 → commit → push → PR → 费枭健检查 → 合并”。这与 GitHub 官方的分支协作流程一致。[GitHub flow](https://docs.github.com/en/get-started/using-github/github-flow)

任务分支可命名为 `docs/bzj-run-notes`、`docs/lgj-run-notes` 或 `fix/save-note`。名称描述本次工作，每个独立任务使用一个分支。

原版运行用的 `1fa9ebb` 是历史基线。用于运行基线的 detached worktree 继续保留该版本，准备提交当前材料时，在正常克隆目录基于最新 main 创建任务分支。

记录按真实任务填写。编译运行已有维护版对应“运行验证”，亲自实现并提交代码对应“开发实现”。提交信息写清实际改动，便于与课程证据对应。

## 3. 先确认有没有写权限

仓库公开后，其他人可以查看和克隆。向原仓库推送分支需要写权限。

若采用直接协作，费枭健在仓库 Settings → Collaborators 中添加队友，队友接受邀请后操作。邀请由负责人按实际协作需要发起。

已有写权限按第4节操作。没有写权限按第7节 Fork 流程操作。

## 4. 有写权限时，队友怎样提交

下面在 PowerShell 中执行，路径选择自己电脑上的实际项目位置。首次登录 GitHub 时按 Git Credential Manager 的提示完成浏览器认证。GitHub HTTPS 推送使用认证凭据或令牌。

### 第一次获取项目

```powershell
git clone https://github.com/xiaojian-fei/micode-notes-course.git
cd micode-notes-course
```

已克隆的项目直接进入已有目录。用下面命令确认当前位置和远程地址。

```powershell
git status
git remote -v
```

旧地址需要更新时执行以下命令。

```powershell
git remote set-url origin https://github.com/xiaojian-fei/micode-notes-course.git
```

### 配置自己的提交身份

在这个仓库中设置本人姓名和本人 GitHub 邮箱，替换示例值。保护邮箱时可使用 GitHub Settings → Emails 显示的本人 noreply 地址。

```powershell
git config user.name "自己的名字"
git config user.email "自己的GitHub邮箱"
```

### 更新 main 并创建分支

先运行 `git status`。工作区干净时继续下面的命令。已有未提交修改时，按第8节先处理。

```powershell
git switch main
git pull --ff-only origin main
git switch -c docs/bzj-run-notes
```

梁冠杰可将分支名换为 `docs/lgj-run-notes`。之后的 push 命令也使用同一个分支名。

### 修改、检查、提交、上传

例如新增 `docs/team/bzj-run-summary.md`，填写本人实际环境、版本、执行过程和结果。自己的原始日志与截图按小组要求私下交接，公开摘要注意去除个人敏感信息。

```powershell
git status
git diff
git add -- docs/team/bzj-run-summary.md
git diff --cached
git commit -m "补充本人原版编译运行摘要"
git push -u origin docs/bzj-run-notes
```

文件路径替换为自己实际新增或修改的文件。`git diff` 查看已跟踪文件的未暂存修改，新文件先打开阅读；`git diff --cached` 查看即将提交的全部改动。

若提交的是程序修改，先按 README 构建并验证相关功能，再在 PR 中填写实际验证结果。

## 5. 在 GitHub 创建 PR

1. 打开原仓库页面，点击 Compare & pull request，或进入 Pull requests → New pull request。
2. 确认 base 为 `main`，compare 为自己的任务分支。
3. 查看 Files changed，确认本次文件和差异。
4. 填写标题和说明，再点 Create pull request。
5. 将 PR 链接发给费枭健。

说明可以按下面的模板填写。

```text
本次改动
补充了哪些文件，实际承担了什么工作。

验证情况
运行的版本、构建结果、实际做过的操作及观察。
尚未执行的验证如实列出。

证据位置
公开摘要路径，或已私下交接的个人记录包名称。
```

PR 创建后继续修改，只需在原任务分支重新 add、commit、push，现有 PR 会自动更新。

## 6. 费枭健怎样接收与合并

1. 打开 PR 的 Files changed，检查修改是否符合任务范围。
2. 文档改动核对版本、命令、链接和实际结果。程序改动检查代码，并按风险运行构建及相关测试。
3. 有问题就在 PR 中指出，队友继续提交到该分支。
4. 确认后点击 Merge pull request，再点击 Confirm merge。以页面开放的合并方式和仓库规则为准。
5. 完成后可点 Delete branch。PR 和已有提交历史仍可查看。

这里新增改动的检查针对本次 PR。已有完整课程回归记录可以继续引用。

合并后，每个人将本地 main 更新。

```powershell
git switch main
git pull --ff-only origin main
```

下一项任务从更新后的 main 新建分支。

## 7. 没有写权限时，用 Fork

1. 在原仓库页面点 Fork，在本人账号创建副本。
2. 克隆自己的 Fork，配置原仓库为 upstream。
3. 同步原仓库 main 后，创建任务分支并修改。

```powershell
git clone https://github.com/你的用户名/micode-notes-course.git
cd micode-notes-course
git remote add upstream https://github.com/xiaojian-fei/micode-notes-course.git
git fetch upstream
git switch main
git merge --ff-only upstream/main
git switch -c docs/my-run-notes
```

修改完成后按第4节检查、add 和 commit，再推送到自己的 Fork。

```powershell
git push -u origin docs/my-run-notes
```

在 GitHub 创建跨仓库 PR，base repository 选 `xiaojian-fei/micode-notes-course`、base 选 `main`，head repository 选自己的 Fork、compare 选任务分支。之后由费枭健检查合并。

## 8. 常见情况怎样处理

| 情况 | 处理方式 |
| --- | --- |
| 已经改了文件，才想起建分支 | 在当前工作区先 `git switch -c 新分支名`，未提交修改通常会一起保留，再检查和提交 |
| 已经在本地 main 提交，尚未 push | 先创建任务分支并推送，在操作旧 main 前交给负责人确认历史 |
| push 提示 Permission denied 或 403 | 核对当前登录账号、协作者权限、邀请是否接受，或采用 Fork |
| push 提示 non-fast-forward | 先 `git fetch origin` 检查同一分支的远程更新，确认后整合，再正常推送 |
| pull --ff-only 失败 | 保留现场，查看本地是否有额外提交，请负责人协助确认整合方式 |
| 提示 uncommitted changes | 用 `git status`、`git diff` 检查自己的工作，保存为明确的提交后再切换或更新 |
| PR 有冲突 | 在任务分支获取最新 main，整合后逐文件解决冲突、验证、提交并推送 |
| 本机 SDK、签名和其他人不同 | 本地环境按 README 配置，提交前检查暂存差异，签名材料私下管理 |

解决冲突时可在工作区干净的任务分支执行 `git fetch origin`、`git merge origin/main`。出现冲突后编辑标记为冲突的文件，确定最终内容，再 add、commit、push。拿不准时保留冲突现场交给负责人确认。

公开提交前用 `git diff --cached` 检查文件。源码和整理后的说明适合进入仓库，`local.properties`、SDK、模拟器、构建缓存、密钥、个人数据库和含敏感信息的原始日志采用各自的本地管理方式。

## 9. 给队友的最短操作清单

确认真实任务 → 获取最新 main → 创建本人任务分支 → 完成修改并验证 → 选择文件提交 → push 分支 → 提交 PR → 处理反馈 → 负责人合并。

采用分支和 PR 后，每次改动的文件、提交人、检查意见和合并结果都能在仓库中查看。
