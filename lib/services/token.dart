// NOTA DE SEGURANÇA: Este código usa SharedPreferences que armazena
// dados em plain text. Para produção, usar flutter_secure_storage:
// https://pub.dev/packages/flutter_secure_storage
//
// Exemplo seguro:
// final storage = FlutterSecureStorage();
// await storage.write(key: 'token', value: token);

import 'package:shared_preferences/shared_preferences.dart';

class TokenWrapper {
  static Future<String> getUserAsync() async {
    SharedPreferences prefs = await SharedPreferences.getInstance();
    String result = prefs.getString('user') ?? '';
    return result;
  }

  static Future<String> getTokenAsync() async {
    SharedPreferences prefs = await SharedPreferences.getInstance();
    String result = prefs.getString('token') ?? '';
    return result;
  }

  static void setToken(String user, String token) {
    SharedPreferences.getInstance().then((SharedPreferences prefs) {
      prefs.setString('user', user);
      prefs.setString('token', token);
    });
  }

  static void clear() {
    SharedPreferences.getInstance().then((SharedPreferences prefs) {
      prefs.remove('user');
      prefs.remove('token');
    });
  }
}
