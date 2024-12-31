// userPersist.js
export const userPersist = store => {
    // 应用初始化时，从 localStorage 中加载用户数据
    const user = localStorage.getItem('user');
    const expiryTime = localStorage.getItem('userExpiry');

    if (user && expiryTime) {
        const now = new Date();
        if (now < new Date(expiryTime)) {
            store.commit('setUser', JSON.parse(user));
        }
    }

    // 监听用户数据的变化
    store.subscribe((mutation, state) => {
        if (mutation.type === 'setUser') {
            const now = new Date();
            const expiryTime = new Date(now.getTime() + 30*60000); // 30 分钟后过期
            localStorage.setItem('user', JSON.stringify(state.user));
            localStorage.setItem('userExpiry', expiryTime);
        }
    });
};
