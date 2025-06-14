# 勤怠管理システム

## 概要

このプロジェクトは、Spring Bootをベースとした勤怠管理アプリケーションです。
従業員はウェブブラウザを通じて出退勤の打刻ができ、管理者は全従業員の勤怠状況を一覧で確認することができます。 また、退勤打刻を忘れた従業員に対して、自動で通知メールを送信する機能も備えています。

## 主な機能

### 一般ユーザー向け機能
* **ユーザー登録・ログイン:** アカウントを作成し、システムにログインします。
* **出退勤打刻:** ボタンクリックで簡単に出勤・退勤時刻を記録します。
* **勤怠状況の確認:** 今日の勤務状況（出勤中、退勤済など）をリアルタイムで確認できます。
* **勤怠履歴の表示:** 月単位で過去の勤怠履歴を一覧で確認できます。
* **定時時刻の設定:** ユーザー登録時に、個人の定時（勤務開始・終了時刻）を設定できます。

### 管理者向け機能
* **管理者アカウント登録:** 管理者権限を持つアカウントを作成します。
* **全従業員の勤怠一覧:** すべてのユーザーの勤怠データを一覧で確認・管理できます。
* **打刻漏れ通知:** 退勤打刻がない従業員がいる場合、管理者にもサマリーメールが送信されます。

### 自動化機能
* **退勤打刻漏れアラート:** スケジュールされたバッチ処理により、終業時刻を過ぎても退勤打刻がないユーザーを検知し、本人と管理者に自動でメール通知を送信します。

## 使用技術

このプロジェクトでは、以下の技術を使用しています。

* **バックエンド:**
    * Java 21
    * Spring Boot 3.4.5
        * Spring Web
        * Spring Data JPA
        * Spring Security
        * Spring Boot Starter Mail
        * Spring Batch
* **データベース:**
    * PostgreSQL
* **ビルドツール:**
    * Maven
* **その他:**
    * Lombok
    * Apache Commons CSV (CSV出力用)

## システム要件

* JDK 21
* Maven 3.3+
* PostgreSQL

## セットアップと実行方法

### 1. データベースの準備
PostgreSQLデータベースに、`kintaiapp`という名前のデータベースを作成してください。

### 2. 設定の構成
`src/main/resources/application.properties` ファイルを、お使いの環境に合わせて編集します。

#### データベース接続設定
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/kintaiapp
spring.datasource.username=あなたのPostgreSQLユーザー名
spring.datasource.password=あなたのPostgreSQLパスワード
spring.datasource.driver-class-name=org.postgresql.Driver
```

#### メールサーバー設定 (Gmailの例)
打刻漏れ通知機能を使用するには、メールサーバーの設定が必要です。
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=あなたのGmailアドレス
spring.mail.password=あなたのGmailアプリパスワード
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```
※Gmailの場合、セキュリティの観点から2要素認証を設定の上、「アプリパスワード」を取得して使用することを推奨します。

### 3. アプリケーションのビルドと実行

ターミナルでプロジェクトのルートディレクトリに移動し、以下のMavenコマンドを実行します。

**ビルド:**
```bash
mvn clean install
```

**実行:**
```bash
mvn spring-boot:run
```

アプリケーションが起動したら、以下のURLにアクセスできます。

* **一般ユーザー登録ページ:** `http://localhost:8080/register.html`
* **ログインページ:** `http://localhost:8080/login.html`
* **管理者登録ページ:** `http://localhost:8080/registerAdmin.html`

## APIエンドポイント (概要)

| メソッド | パス                               | 説明                                   | 権限       |
| :------- | :--------------------------------- | :------------------------------------- | :--------- |
| `POST`   | `/api/auth/signup`                 | 新しい一般ユーザーを登録します。         | 誰でも     |
| `POST`   | `/api/auth/login`                  | ログインして認証情報を取得します。     | 誰でも     |
| `POST`   | `/api/attendance/clock-in/{userId}`  | 出勤打刻を記録します。               | ユーザー     |
| `POST`   | `/api/attendance/clock-out/{userId}` | 退勤打刻を記録します。               | ユーザー     |
| `GET`    | `/api/attendance/{userId}/status`  | 現在の勤務状況を取得します。           | ユーザー     |
| `GET`    | `/api/attendance/monthly/{userId}` | 指定した月の勤怠履歴を取得します。       | ユーザー     |
| `POST`   | `/api/auth/admin/signup`           | 新しい管理者を登録します。             | 誰でも     |
| `GET`    | `/api/admin/attendance`            | 全ユーザーの勤怠履歴を取得します。       | 管理者     |

## ディレクトリ構造

```
kinntai_app
├── src
│   ├── main
│   │   ├── java/com/example/kinntai
│   │   │   ├── KintaiApplication.java      # Spring Boot起動クラス
│   │   │   ├── batch                     # バッチ処理関連 (打刻漏れ通知)
│   │   │   ├── config                    # セキュリティ設定など
│   │   │   ├── controller                # APIリクエストを処理するコントローラー
│   │   │   ├── dto                       # データ転送オブジェクト
│   │   │   ├── entity                    # データベースのテーブルに対応するエンティティ
│   │   │   ├── repository                # データベースアクセスを行うリポジトリ
│   │   │   └── service                   # ビジネスロジックを実装するサービス
│   │   └── resources
│   │       ├── application.properties    # アプリケーション設定ファイル
│   │       ├── static                    # HTML, CSS, JSなどの静的ファイル
│   │       └── excel                     # Excelテンプレート (CSV形式)
│   └── test                              # テストコード
└── pom.xml                               # Mavenプロジェクト設定ファイル
```
