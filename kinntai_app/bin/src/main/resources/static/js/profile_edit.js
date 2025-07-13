// src/main/resources/static/js/profile_edit.js

// 現在ログインしているユーザーの情報をロードし、フォームに表示する
async function loadUserProfile() {
    console.log('ユーザープロフィールデータをロード中...');
    try {
        const response = await fetch('/api/users/me'); // ログインユーザーの情報を取得するAPI
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(`プロフィールのロードに失敗しました: ${errorData.message || response.statusText}`);
        }
        const user = await response.json();
        console.log('取得したユーザープロフィール:', user);

        document.getElementById('profileUserId').value = user.id || '';
        document.getElementById('profileUsername').value = user.username || '';
        document.getElementById('profileEmail').value = user.email || '';
        document.getElementById('profileDefaultStartTime').value = user.defaultStartTime || '';
        document.getElementById('profileDefaultEndTime').value = user.defaultEndTime || '';

    } catch (error) {
        console.error('ユーザープロフィールのロード中にエラーが発生しました:', error);
        showProfileMessage('ユーザープロフィールのロードに失敗しました。', 'error');
    }
}

// プロフィール更新フォームの送信処理
async function saveUserProfile(event) {
    event.preventDefault(); // フォームのデフォルト送信を防ぐ
    console.log('ユーザープロフィールを保存中...');
    showProfileMessage('保存中...', 'info');

    const userId = document.getElementById('profileUserId').value;
    if (!userId) {
        showProfileMessage('ユーザーIDが見つかりません。保存できません。', 'error');
        console.error('ユーザーIDがフォームに設定されていません。');
        return;
    }

    const username = document.getElementById('profileUsername').value;
    const email = document.getElementById('profileEmail').value;
    const defaultStartTime = document.getElementById('profileDefaultStartTime').value;
    const defaultEndTime = document.getElementById('profileDefaultEndTime').value;

    // バリデーション（簡易的な例）
    if (!username || !email) {
        showProfileMessage('ユーザー名とメールアドレスは必須です。', 'error');
        return;
    }
    if (!/^[^@]+@[^@]+\.[^@]+$/.test(email)) {
        showProfileMessage('有効なメールアドレスを入力してください。', 'error');
        return;
    }

    const userData = {
        username: username,
        email: email,
        defaultStartTime: defaultStartTime || null,
        defaultEndTime: defaultEndTime || null
    };

    try {
        const response = await fetch(`/api/users/${userId}`, {
            method: 'PUT', // または PATCH
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(userData)
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(`プロフィールの保存に失敗しました: ${errorData.message || response.statusText}`);
        }

        const updatedUser = await response.json();
        console.log('プロフィールが正常に保存されました:', updatedUser);
        showProfileMessage('プロフィールが正常に更新されました！', 'success');

    } catch (error) {
        console.error('ユーザープロフィールの保存中にエラーが発生しました:', error);
        showProfileMessage('ユーザープロフィールの保存に失敗しました。', 'error');
    }
}

// メッセージ表示ヘルパー関数
function showProfileMessage(message, type) {
    const msgElement = document.getElementById('profileMessage');
    if (msgElement) {
        msgElement.textContent = message;
        msgElement.style.color = type === 'error' ? 'red' : (type === 'success' ? 'green' : 'blue');
    }
}

// ページ読み込み完了時にユーザープロフィールをロード
document.addEventListener('DOMContentLoaded', () => {
    loadUserProfile(); // ページロード時にユーザープロフィールをロード

    const profileForm = document.getElementById('userProfileForm');
    if (profileForm) {
        profileForm.addEventListener('submit', saveUserProfile);
    }
});