import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:todoapp/model/list_model.dart';
import 'package:todoapp/model/todo_model.dart';
import 'package:todoapp/services/logger.dart';
import 'package:todoapp/services/token.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';

class TodoService {
  // Configuração via .env
  static String host = dotenv.env['HOST'] ?? 'localhost';
  static String port = dotenv.env['PORT'] ?? '7100';

  static String get _baseUrl => 'http://$host:$port';

  static Future<String?> login(String username, String password) async {
    try {
      final uri = Uri.parse('$_baseUrl/login');
      http.Response resp = await http.post(
        uri,
        headers: const <String, String>{
          'Content-Type': 'application/json; charset=UTF-8',
        },
        body: jsonEncode(<String, String>{
          'username': username,
          'password': password,
        }),
      );

      LoggerService.info('POST $uri -> ${resp.statusCode}');
      LoggerService.info(resp.body);

      if (resp.statusCode == 200) {
        return resp.body; // token no formato "Bearer <jwt>"
      }
    } catch (e) {
      LoggerService.error(e);
    }
    return null;
  }

  static Future<List<TodoListModel>> getAllLists() async {
    final String token = await TokenWrapper.getTokenAsync();
    if (token.isEmpty) {
      LoggerService.info('Token vazio — faça login primeiro.');
      return List.empty();
    }

    final Uri uri = Uri.parse('$_baseUrl/todolist');
    LoggerService.info('GET $uri with token <$token>');

    try {
      final http.Response resp = await http.get(
        uri,
        headers: <String, String>{
          'Authorization': token,
        },
      );

      LoggerService.info('Response (${resp.statusCode}): ${resp.body}');

      if (resp.statusCode == 200) {
        final List<dynamic> lists = jsonDecode(resp.body);
        return lists.map((list) => TodoListModel.fromJson(list)).toList();
      }
    } catch (e) {
      LoggerService.error(e);
    }

    return List.empty();
  }

  static Future<List<TodoModel>> getAllTasks(int listId) async {
    final String token = await TokenWrapper.getTokenAsync();
    if (token.isEmpty) {
      LoggerService.info('Token vazio — faça login primeiro.');
      return List.empty();
    }

    final Uri uri = Uri.parse('$_baseUrl/todo/$listId/tasks');
    LoggerService.info('GET $uri with token <$token>');

    try {
      final http.Response resp = await http.get(
        uri,
        headers: <String, String>{
          'Authorization': token,
        },
      );

      LoggerService.info('Response (${resp.statusCode}): ${resp.body}');

      if (resp.statusCode == 200) {
        final List<dynamic> tasks = jsonDecode(resp.body);
        return tasks.map((task) => TodoModel.fromJson(task)).toList();
      }
    } catch (e) {
      LoggerService.error(e);
    }

    return List.empty();
  }
}
