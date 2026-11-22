# Робота з Git у команді

## Гілки

| Гілка | Призначення |
| --- | --- |
| `main` | стабільна версія; прямі коміти заборонені |
| `feature/<модуль>` | розробка одного модуля одним учасником |
| `fix/<опис>` | виправлення помилок |

Початкові гілки створює `bash scripts/setup-branches.sh`.

## Перший запуск репозиторію

```bash
git init -b main
git add .
git commit -m "chore: базова структура проєкту"
git remote add origin https://github.com/<організація>/<репозиторій>.git
git push -u origin main
bash scripts/setup-branches.sh
git push --all origin
```

## Робочий цикл учасника

```bash
git checkout feature/<модуль>
git pull origin main --rebase
# ... зміни ...
git add -A
git commit -m "feat(settlement): перевірка зайнятості місця"
git push origin feature/<модуль>
# далі — Pull Request у main на GitHub
```

## Правила Pull Request

1. Опис: що змінено, які вимоги (FR/NFR) закриває.
2. Код-рев'ю від щонайменше одного іншого учасника.
3. Перед злиттям: `mvn test` без помилок.
4. Злиття — «Squash and merge» або «Merge commit»; після злиття гілка видаляється, якщо завдання завершене.

## Формат повідомлень комітів

`тип(модуль): короткий опис`, типи: `feat`, `fix`, `docs`, `test`, `refactor`, `chore`.

## Налаштування захисту main (GitHub → Settings → Branches)

- Require a pull request before merging
- Require approvals: 1
- Do not allow bypassing the above settings
