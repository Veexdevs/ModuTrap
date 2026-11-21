# ModuTrap — Bloxstrap Mobile

**Bloxstrap Mobile** (APK: **ModuTrap**) é um gerenciador e inicializador **independente** do Roblox para Android, inspirado no Bloxstrap original para Windows. O app aplica configurações de gráficos/desempenho como **Fast Flags**, gerencia presets e backups, oferece personalização visual e integra-se opcionalmente ao **Shizuku** para operações com privilégios.

> **O Roblox é um software proprietário.** Este projeto **não inclui e não redistribui** o APK do Roblox. O cliente oficial deve ser instalado separadamente, a partir da loja oficial. O ModuTrap atua apenas como gerenciador/iniciador e never substitui arquivos protegidos do Roblox.

---

## Recursos

### Início (Home)
- Status do Roblox (instalado, versão, data dir, build) com detecção em múltiplas camadas.
- Botão **INICIAR ROBLOX** (através de intents oficiais) e versão/build detectadas.
- Status do Shizuku (pronto, em execução, aguardando autorização, não instalado).
- Acesso direto às Configurações e abertura da página oficial de instalação do Roblox.

### Configurações
- **Gráficos**: qualidade gráfica, distância de renderização, MSAA, sombras, pós-processamento, textures de terreno e resolução dinâmica.
- **Desempenho**: limite de FPS, otimização de GC (Luau), redução de animações, limpeza de cache e limite de processos em segundo plano.
- **Iniciação**: escolha do pacote do Roblox (global, VNG ou personalizado), aplicação de Fast Flags/otimizações e histórico da última inicialização.
- **Personalização**: temas, acento, presets visuais e gerenciamento de arquivos próprios.

### Fast Flags
- Lista pesquisável de Fast Flags com adicionar/editar/excluir.
- **Importar/Exportar JSON** no formato `{ "FFlags": { "Nome": "Valor" } }`.
- **Presets** (desempenho equilibrado, máximo de FPS, gráficos baixos, privacidade, rede).
- **Backups** de cria/restaura/exclui.
- **Honestidade técnica**: apenas flags da lista de permitidas do Roblox são marcadas como verificadas. Flags desconhecidas são armazenadas, mas marcadas como *não verificadas* e o app avisa que podem não funcionar.

### Integrações (Shizuku — opcional)
- Detecção de instalado/em execução, solicitação de autorização e verificação de privilégios (UID, versão).
- Diagnóstico gerável com relatório de dispositivo, Roblox, Shizuku e flags.
- O app **continua funcionando como inicializador mesmo sem Shizuku**. Não há tentativa de contornar segurança, anti-cheat ou restrições do Roblox.

### Sobre
- Versão, créditos, licenças e link do repositório.

### Personalização
- Tema (sistema/escuro/claro), acento (presets + hex customizado).
- Presets visuais salvos/aplicáveis e gerenciamento de arquivos próprios do app.
- Backup completo de configurações e Fast Flags.

---

## Stack técnica

- **Kotlin** + **Jetpack Compose** (Material Design 3)
- **Gradle Kotlin DSL** com version catalog (`gradle/libs.versions.toml`)
- **DataStore** (Preferences) para configurações
- **Kotlin Coroutines** e **Serialization**
- **Shizuku API** para integração opcional
- **Android SDK**: `minSdk 26`, `compileSdk/targetSdk 35`, Java 17
- **GitHub Actions** para build automatizado

Não há HTML/CSS/JS — é um app Android nativo.

---

## Estrutura do projeto

```
app/src/main/kotlin/com/modutrap/
├── core/            # AppPaths, Logger, AppContainer (DI manual)
├── utils/           # JsonUtils, FileUtils
├── models/          # Modelos de dados (FFlagEntry, RobloxStatus, LaunchResult, …)
├── settings/        # AppSettings (@Serializable), SettingsRepository
├── data/            # SettingsDataStore (DataStore)
├── flags/           # FastFlagCatalog, FastFlagValidator
├── repositories/    # FFlagRepository, PresetRepository, PersonalizationRepository
├── services/        # FastFlagApplier, RobloxLauncher, DiagnosticsService
├── roblox/          # RobloxManager (detecção/launch/página de instalação)
├── shizuku/         # ShizukuManager (bridge via AIDL)
├── integrations/    # ShizukuIntegration
├── personalization/ # ThemeManager, BackupManager
├── ui/
│   ├── theme/       # Color, Shape, Type, Theme
│   ├── components/  # Componentes reutilizáveis
│   ├── screens/     # 9 telas (home, settings, flags, personalization, integrations, about)
│   └── viewmodels/  # ViewModels
├── navigation/      # Routes, AppNavGraph
├── BloxstrapMobileApp.kt  # Application
└── MainActivity.kt         # Activity única que hospeda AppRoot
```

A arquitetura separa UI, navegação, dados, modelos, repositórios, serviços e integrações. `MainActivity` é apenas um host mínimo — não contém a lógica da app.

---

## Como buildar

### Pré-requisitos
- JDK 17
- Android SDK com `platforms;android-35` e `build-tools;35.0.0`
- (opcional) Shizuku instalado no dispositivo para recursos com privilégios

### Debug
```bash
./gradlew :app:assembleDebug
```
O APK fica em `app/build/outputs/apk/debug/app-debug.apk`.

### Release
```bash
./gradlew :app:assembleRelease
```
Sem um keystore, o build release usa a assinatura de debug (still installable). Para uma release assinada, veja a seção de CI.

### CI (GitHub Actions)
O workflow `.github/workflows/android.yml` roda em:
- `push` para `main`
- `pull_request` para `main`
- `workflow_dispatch` (manual)

Ele monta o JDK 17 + Android SDK, roda `assembleDebug` e publica `app-debug.apk` como artefato.

**Release assinada opcional:** o job `release` roda `assembleRelease` apenas quando o segredo `RELEASE_KEYSTORE_BASE64` (keystore em base64) está presente, juntamente com `KS_PASS`, `KS_KEY_PASS` e `KS_ALIAS`. Nenhuma senha ou chave é commitada no código — tudo é fornecido por segredos do GitHub Actions.

---

## Integração com o Roblox

- O app detecta `com.roblox.client` (global) e `com.roblox.client.vnggames` (VNG).
- Abre o Roblox via intents oficiais e mostra mensagens claras quando não instalado.
- As configurações de gráficos/desempenho são traduzidas em Fast Flags e aplicadas via `ClientAppSettings.json` (escrevendo no diretório de dados do Roblox, via Shizuku quando disponível).
- Quando Shizuku/root não está disponível, o app informa que as flags não foram aplicadas, mas a inicialização continua.

## Integração com o Shizuku

- Usa a API pública do Shizuku (`Shizuku.pingBinder()`, `getVersion()`, `checkSelfPermission()`, `getUid()`) e executa shell via AIDL `IShizukuService.newProcess`.
- Requer autorização do usuário no Shizuku. Opera apenas tarefas permitidas e necessárias (gravar `ClientAppSettings.json` e diagnósticos).
- **Não** contorna mecanismos de segurança, anti-cheat ou restrições do Roblox.

## Licenças

Open Source — **MIT**.

O Roblox é propriedade da Roblox Corporation. Este projeto não tem afiliação com a Roblox. Bloxstrap, Chipstrap, FastStrap, Chevvy e Shizuku são projetos de terceiros usados como referência técnica.
